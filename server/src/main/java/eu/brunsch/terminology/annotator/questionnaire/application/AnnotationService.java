package eu.brunsch.terminology.annotator.questionnaire.application;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import eu.brunsch.terminology.annotator.questionnaire.domain.Annotation;
import eu.brunsch.terminology.annotator.questionnaire.domain.AnnotationDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.AnnotationMapper;
import eu.brunsch.terminology.annotator.questionnaire.domain.Choice;
import eu.brunsch.terminology.annotator.questionnaire.domain.ChoiceDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.ChoiceMapper;
import eu.brunsch.terminology.annotator.questionnaire.domain.Comment;
import eu.brunsch.terminology.annotator.questionnaire.domain.CommentDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.CommentMapper;
import eu.brunsch.terminology.annotator.questionnaire.domain.DataElement;
import eu.brunsch.terminology.annotator.questionnaire.domain.DataElementDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.DataElementMapper;
import eu.brunsch.terminology.annotator.questionnaire.domain.Questionnaire;
import eu.brunsch.terminology.annotator.questionnaire.domain.QuestionnaireDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.QuestionnaireMapper;
import eu.brunsch.terminology.annotator.questionnaire.domain.QuestionnaireOptions;
import eu.brunsch.terminology.annotator.questionnaire.domain.Version;
import eu.brunsch.terminology.annotator.questionnaire.domain.VersionCreateDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.VersionDto;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.AnnotationRepository;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.ChoiceRepository;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.CommentRepository;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.DataElementRepository;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.QuestionnaireRepository;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.VersionRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AnnotationService {
    /** Questionnaire DB access. */
    private final QuestionnaireRepository questionnaireRepository;
    /** Version DB access. */
    private final VersionRepository versionRepository;
    /** Data Element DB access. */
    private final DataElementRepository dataElementRepository;
    /** Choice DB access. */
    private final ChoiceRepository choiceRepository;
    /** Annotation DB access. */
    private final AnnotationRepository annotationRepository;
    /** Comment DB access. */
    private final CommentRepository commentRepository;

    /** Map Questionnaire DTOs to Questionnaire entities (and vice versa). */
    private final QuestionnaireMapper questionnaireMapper;
    /** Map Data Element DTOs to Data Element entities (and vice versa). */
    private final DataElementMapper dataElementMapper;
    /** Map Choice DTOs to Choice entities (and vice versa). */
    private final ChoiceMapper choiceMapper;
    /** Map Annotation DTOs to Annotation entities (and vice versa). */
    private final AnnotationMapper annotationMapper;
    /** Map Comment DTOs to Comment entities (and vice versa). */
    private final CommentMapper commentMapper;


    //////////////////////////
    /// QUESTIONNAIRE CRUD ///
    //////////////////////////

    /**
     * Create a new questionnaire, incl. its data elements.
     * @param dto probably the parsed eCRF
     * @return ID of the created questionnaire. NO IDs of data elements.
     */
    public Long importQuestionnaire(final QuestionnaireDto dto) {
        final Questionnaire questionnaire = questionnaireMapper.toEntity(dto);
        questionnaireRepository.save(questionnaire);
        for (final DataElementDto dDto : dto.getDataElements()) {
            Choice[] choices = null;
            if (dDto.getChoices() != null) {
                choices = new Choice[dDto.getChoices().length];
                for (int i = 0; i < dDto.getChoices().length; i++) {
                    final ChoiceDto cDto = dDto.getChoices()[i];
                    final Choice choice = choiceMapper.toEntity(cDto);
                    choices[i] = choice;
                    choiceRepository.save(choice);
                }
            }
            final DataElement dataElement = dataElementMapper.toEntity(dDto);
            dataElement.setQuestionnaire(questionnaire);
            dataElement.setChoices(choices);
            dataElementRepository.save(dataElement);
        }
        return questionnaire.getId();
    }

    /**
     * Delete a questionnaire.
     * DOES NOT DELETE THE ANNOTATIONS OR DATA ELEMENTS!
     * We want to archive them for suggestions.
     * @param id ID of the questionnaire.
     */
    public void deleteQuestionnaire(final Long id) {
        final Questionnaire q = questionnaireRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException());
        final var dataElements = dataElementRepository.findByQuestionnaire(q);
        for (final DataElement dataElement : dataElements) {
            dataElement.setQuestionnaire(null);
            dataElementRepository.save(dataElement);
        }
        questionnaireRepository.delete(q);
    }

    /**
     * Get basic info about all collections this user has access to.
     * @return DTOs without data elements.
     */
    public QuestionnaireDto[] getAllQuestionnaires() {
        return questionnaireRepository.findAll().stream()
            .filter(q -> q.seenBy(username()))
            .map(questionnaireMapper::toDto)
            .toArray(QuestionnaireDto[]::new);
    }

    /**
     * Get a questionnaire incl. all its data elements, annotations, comments.
     * @param id Internal ID of the questionnaire
     * @return fully-filled questionnaire dto
     */
    public QuestionnaireDto getQuestionnaire(final Long id) {
        final Questionnaire q = questionnaireRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!q.seenBy(username())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        final var dataElements = dataElementRepository.findByQuestionnaire(q);
        final QuestionnaireDto questionnaireDto = questionnaireMapper.toDto(q);
        final DataElementDto[] dataElemetDtos = dataElements.stream()
            .map(dataElementMapper::toDto)
            .map(d -> {
                final var annotations = getDataElementsAnnotations(d.getId());
                d.setAnnotations(annotations);
                return d;
            }).toArray(DataElementDto[]::new);
        questionnaireDto.setDataElements(dataElemetDtos);
        return questionnaireDto;
    }


    /////////////////////////
    /// DATA ELEMENT READ ///
    ////////////////////////

    /**
     * Get a single data element by its ID.
     * Throws an exception when the user is unable to see it.
     * Private method!
     * @param id Internal ID.
     * @return data element, excluding comments
     */
    @Transactional(readOnly = true)
    private DataElement getDataElement(final Long id) {
        final DataElement variable = dataElementRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // check if user is eligible to see this data element
        // -> must not be associated with a questionnaire not seen by this user
        final Questionnaire q = variable.getQuestionnaire();
        if (q != null && !q.seenBy(username())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return variable;
        }
    }

    /**
     * Get a single data element by its ID.
     * @param id Internal ID.
     * @return data element, including its comments.
     */
    @Transactional(readOnly = true)
    public DataElementDto getDataElementWithComments(final Long id) {
        final DataElement variable = getDataElement(id);
        final DataElementDto dto = dataElementMapper.toDto(variable);
        final AnnotationDto[] annotations = getDataElementsAnnotations(id);
        dto.setAnnotations(annotations);
        return dto;
    }

    ///////////////////////
    /// ANNOTATION CRUD ///
    ///////////////////////

    /**
     * Get all annotations on a given data element.
     * @param dataElementId Internal ID of the data element
     * @return all annotations on the data element, regardless of status
     */
    @Transactional(readOnly = true)
    public AnnotationDto[] getDataElementsAnnotations(
        final Long dataElementId
    ) {
        final DataElement d = getDataElement(dataElementId);
        return annotationRepository.findByVariable(d)
            .stream()
            .filter(a -> a.seenBy(username()))
            .map(annotationMapper::toDto)
            .toArray(AnnotationDto[]::new);
    }

    /**
     * Add annotation to data element.
     * @param dataElementId Internal ID of the annotated data element
     * @param dto information by which to create the annotation
     * @return ID of the created annotation
     */
    public Long createAnnotation(
        final Long dataElementId,
        final AnnotationDto dto
    ) {
        final Annotation annotation = annotationMapper.toEntity(dto);
        final DataElement parent = getDataElement(dataElementId);

        // check if we're allowed to annotate
        if (!parent.annotatableBy(username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // fill DTO
        annotation.setVariable(parent);
        annotationRepository.save(annotation);
        return annotation.getId();
    }

    /**
     * Delete an annotation and its comments.
     * @param annotationId annotation to remove
     */
    public void removeAnnotation(final Long annotationId) {
        final Annotation annotation = getAnnotation(annotationId);

        // see if user is allowed to delete
        // -> either this is the user's own annotation or user has admin rights
        final Questionnaire q = annotation.getVariable().getQuestionnaire();
        if (!annotation.getAuthorName().equals(username()) && q != null && !q.administeredBy(username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        annotationRepository.deleteById(annotationId);
    }

    /**
     * Get an annotation by ID.
     * Throws exception if the user is unable to see this annotation.
     * @param id Internal ID of the annotation
     * @return annotation
     */
    private Annotation getAnnotation(final Long id) {
        final Annotation annotation = annotationRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // check if user is able to see
        if (!annotation.seenBy(username())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return annotation;
    }

    /**
     * Get an annotation by ID.
     * @param id Internal ID of the annotation
     * @return annotation DTO
     */
    public AnnotationDto getAnnotationDto(final Long id) {
        return annotationMapper.toDto(getAnnotation(id));
    }

    ////////////////////
    /// COMMENT CRUD ///
    ////////////////////

    /**
     * Create a comment and update its annotation's status.
     * @param annotationId annotation the comment is referring to
     * @param dto information about the comment to create
     * @return ID of created comment
     */
    public Long addComment(final Long annotationId, final CommentDto dto) {
        final Annotation parent = annotationRepository.findById(annotationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!parent.seenBy(username())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "did not find annotation with id " + annotationId); 
        }
        if (!parent.reviewableBy(username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN); 
        }

        final Comment comment = commentMapper.toEntity(dto);
        comment.setAnnotation(parent);
        commentRepository.save(comment);
        final QuestionnaireOptions req = parent.getVariable()
            .getQuestionnaire()
            .getOptions();
        parent.addComment(comment, req);
        annotationRepository.save(parent);
        return comment.getId();
    }

    /**
     * Remove a comment.
     * Re-calculates status of the annotation it responds to.
     * @param id Internal ID of the comment to delete
     */
    public void deleteComment(final Long id) {
        final Comment comment = commentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        final Annotation annotation = comment.getAnnotation();
        if (!annotation.seenBy(username())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND); 
        }
        if (!comment.getAuthorName().equals(username()) && annotation.getVariable().getQuestionnaire().administeredBy(username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN); 
        }

        final QuestionnaireOptions req = annotation.getVariable()
            .getQuestionnaire()
            .getOptions();
        annotation.removeComment(comment, req);
        annotationRepository.save(annotation);
        commentRepository.deleteById(id);
    }

    /**
     * Creates a new version.
     * If not all annotations are found, throws 404.
     * @param questionnaireId questionnaire that's versioned
     * @param dto information about the newly created version
     * @return ID of newly created versioning
     * @throws ResponseStatusException 404 if questionnaire or annotation not found or not seen, 401 if user is no admin of the questionnaire
     */
    public Long createVersion(final Long questionnaireId, final VersionCreateDto dto) {
        final String username = username();
        final Questionnaire q = questionnaireRepository.findById(questionnaireId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // access control
        if (!q.seenBy(username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else if (!q.administeredBy(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final Annotation[] annotations = annotationRepository
            .findAllById(List.of(dto.getAnnotationIds()))
            .stream()
            // we already checked for admin, so we don't need to do this step I guess
            // but when we stream anyways ...
            .filter(a -> a.seenBy(username))
            .toArray(size -> new Annotation[size]);
        // check if all annotations exist
        if (annotations.length != dto.getAnnotationIds().length) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // key must be unique for each questionnaire id
        if (versionRepository.existsByKeyAndQuestionnaireId(dto.getKey(), questionnaireId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        final Version version = new Version(null, dto.getKey(), dto.getDescription(), annotations, q, null);
        return versionRepository.save(version).getId();
    }

    public VersionDto getVersion(final Long questionnaireId, final Long versionId) {
        final String username = username();
        final Questionnaire q = questionnaireRepository.findById(questionnaireId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // access control
        if (!q.seenBy(username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else if (!q.administeredBy(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        final VersionDto dto = new VersionDto();
        final Version v = versionRepository.findById(versionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        dto.setAnnotations(
            List.of(v.getAnnotations())
                .stream()
                .map(a -> annotationMapper.toDto(a))
                .toArray(size -> new AnnotationDto[size])
        );
        dto.setKey(v.getKey());
        dto.setDescription(v.getDescription());
        dto.setId(v.getId());
        dto.setSubmittedAt(v.getSubmittedAt());
        return dto;
    }

    /**
     * TODO put where it belongs (if there is a better place)
     * @returns username of current user
     */
    private String username() {
        final Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaimAsString("preferred_username");
    }
}
