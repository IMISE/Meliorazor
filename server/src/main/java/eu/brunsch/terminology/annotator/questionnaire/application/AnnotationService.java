package eu.brunsch.terminology.annotator.questionnaire.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import eu.brunsch.terminology.annotator.questionnaire.domain.Questionnaire;
import eu.brunsch.terminology.annotator.questionnaire.domain.QuestionnaireDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.QuestionnaireMapper;
import eu.brunsch.terminology.annotator.questionnaire.domain.QuestionnaireOptions;
import eu.brunsch.terminology.annotator.questionnaire.domain.Version;
import eu.brunsch.terminology.annotator.questionnaire.domain.VersionCreateDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.VersionDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.Participant.Role;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.Annotatable;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.AnnotatableMapper;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.Choice;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.ChoiceDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.DataElement;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.DataElementDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.RichActionDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.Unit;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.UnitDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.Action;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.ActionMapper;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.Annotation;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.AnnotationDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.AnnotationMapper;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.Code;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.Comment;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.CommentDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.Versioning;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.Annotation.Status;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.ActionRepository;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.AnnotatableRepository;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.AnnotationRepository;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.ChoiceRepository;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.DataElementRepository;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.QuestionnaireRepository;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.UnitRepository;
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
    /** Unit DB access. */
    private final UnitRepository unitRepository;
    /** Annotatable (Data Element, Choice and Unit) unified DB access. */
    private final AnnotatableRepository annotatableRepository;
    /** Annotation DB access. */
    private final AnnotationRepository annotationRepository;
    /** Annotation Action DB access. */
    private final ActionRepository actionRepository;

    /** Map Questionnaire DTOs to Questionnaire entities (and vice versa). */
    private final QuestionnaireMapper questionnaireMapper;
    /** Map Annotatable DTOs to Annotatable entities (and vice versa). */
    private final AnnotatableMapper annotatableMapper;
    /** Map Annotation DTOs to Annotation entities (and vice versa). */
    private final AnnotationMapper annotationMapper;
    /** Map Annotation Action DTOs to Annotation ACtion entities (and vice versa). */
    private final ActionMapper actionMapper;


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
        // check if the first user is us (creator) and has admin privileges
        if (!questionnaire.administeredBy(username()) || !username().equals(questionnaire.getParticipants()[0].getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This participant configuration is forbidden");
        }
        questionnaireRepository.save(questionnaire);
        for (final DataElementDto dDto : dto.getDataElements()) {
            // save DE first, so the choices can reference its ID later
            final DataElement dataElement = annotatableMapper.toEntity(dDto);
            final Unit unit = dataElement.getUnit();
            unitRepository.save(unit);
            dataElement.setQuestionnaire(questionnaire);
            dataElementRepository.save(dataElement);

            unit.setVariable(dataElement);
            unitRepository.save(unit);

            // create & save choices, if required
            if (dDto.getChoices() != null) {

                final Choice[] choices = new Choice[dDto.getChoices().length];
                for (int i = 0; i < dDto.getChoices().length; i++) {
                    final ChoiceDto cDto = dDto.getChoices()[i];
                    final Choice choice = annotatableMapper.toEntity(cDto);
                    choice.setVariable(dataElement);
                    choices[i] = choice;
                    choiceRepository.save(choice);
                }

                // set choices & save again
                dataElement.setChoices(choices);
                dataElementRepository.save(dataElement);
            }
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
        if (!q.administeredBy(username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
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
            .map(this::getDataElementWithComments)
            .toArray(DataElementDto[]::new);
        questionnaireDto.setDataElements(dataElemetDtos);
        return questionnaireDto;
    }

    /**
     * Edit a single questionnaire.
     * @param id identifier of the questionnaire
     * @param dto new contents; ignores ID, data elements and empty (undefined/null) properties.
     */
    public void editQuestionnaire(final Long id, final QuestionnaireDto dto) {
        // access & permissions stuff
        if (dto.getId() != null && id != dto.getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IDs do not match");
        }
        final Questionnaire q = questionnaireRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!q.seenBy(username())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else if (!q.administeredBy(username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
   
        // the real deal
        if (dto.getTitle() != null) {
            q.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            q.setDescription(dto.getDescription());
        }
        if (dto.getParticipants() != null) {
            // participants need to still include ourselves as admins
            // first user (creator) has to stay the same user & must be an admin, the other roles may change
            if (
                Arrays.stream(dto.getParticipants()).anyMatch(p -> p.getUsername().equals(username()) &&
                List.of(p.getRoles()).contains(Role.ADMIN)) && List.of(dto.getParticipants()[0].getRoles()).contains(Role.ADMIN) &&
                dto.getParticipants()[0].getUsername().equals(q.getParticipants()[0].getUsername())
            ) {
                q.setParticipants(dto.getParticipants());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This participant configuration is forbidden");
            }
        }
        if (dto.getValueSets() != null) {
            // need to have at least one valueset
            if (dto.getValueSets().length > 0) {
                q.setValueSets(dto.getValueSets());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Too few ValueSets");
            }
        }
        if (dto.getOptions() != null) {
            dto.setOptions(dto.getOptions());
        }

        questionnaireRepository.save(q);
    }


    /////////////////////////
    /// DATA ELEMENT READ ///
    /////////////////////////

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
        return getDataElementWithComments(variable);
    }

    /**
     * Turn a Data Element into a DTO + get all its annotations, comments etc.
     * @param id Internal ID.
     * @return data element, including its comments.
     */
    @Transactional(readOnly = true)
    public DataElementDto getDataElementWithComments(final DataElement variable) {
        // all annotations visible to user
        final AnnotationDto[] annotationDtos = annotationRepository
            .findByTarget(variable)
            .stream()
            .filter(a -> a.seenBy(username()))
            .map(annotationMapper::toDto)
            .toArray(AnnotationDto[]::new);

        final List<ChoiceDto> choiceDtos = new ArrayList<>();
        // populate choices with their annotations
        for (final Choice choice :  variable.getChoices()) {
            final ChoiceDto choiceDto = annotatableMapper.toDto(choice);
            final List<Annotation> choiceAnnotations = annotationRepository.findByTarget(choice);
            List<AnnotationDto> choiceAnnotationDtos = new ArrayList<>();
            for (final Annotation choiceAnnotation : choiceAnnotations) {
                if (!choiceAnnotation.seenBy(username())) {
                    continue;
                }

                final AnnotationDto choiceAnnotationDto = annotationMapper.toDto(choiceAnnotation);
                choiceAnnotationDto.setActions(
                    annotationMapper.toDtos(
                        choiceAnnotation.getActions(username())));
                choiceAnnotationDtos.add(choiceAnnotationDto);
            }
            choiceDto.setAnnotations(choiceAnnotationDtos.toArray(AnnotationDto[]::new));
            choiceDtos.add(choiceDto);
        }

        final Unit unit = variable.getUnit();
        UnitDto unitDto = null;
        if (unit != null) {
            unitDto = annotatableMapper.toDto(unit);
            unitDto.setAnnotations(
                annotationRepository.findByTarget(unit).stream()
                    .map(annotationMapper::toDto)
                    .toArray(AnnotationDto[]::new));
        }

        // put it all together
        final DataElementDto dto = annotatableMapper.toDto(variable);
        dto.setUnit(unitDto);
        dto.setChoices(choiceDtos.toArray(ChoiceDto[]::new));
        dto.setAnnotations(annotationDtos);
        return dto;
    }

    ///////////////////////
    /// ANNOTATION CRUD ///
    ///////////////////////

    /**
     * Get all annotations on a given data element as DTOs.
     * @param dataElementId Internal ID of the data element
     * @return all annotations on the data element, regardless of status
     */
    @Transactional(readOnly = true)
    public AnnotationDto[] getDataElementsAnnotations(
        final Long dataElementId
    ) {
        final DataElement d = getDataElement(dataElementId);
        return annotationRepository.findByTarget(d)
            .stream()
            .filter(a -> a.seenBy(username()))
            .map(annotationMapper::toDto)
            .toArray(AnnotationDto[]::new);
    }

    /**
     * Add an annotation to an annotatable element.
     * @param annotatableElementId Internal ID of the annotated element
     * @param dto choice by which to create the annotation
     * @return ID of the created annotation
     */
    public Long createAnnotation(
        final Long annotatableElementId,
        final AnnotationDto dto
    ) {
        // must contain exactly one submission action with a code attached
        final Code code = dto.getCode();
        if (
            code == null ||
            dto.getActions() == null ||
            dto.getActions().length != 1 ||
            dto.getActions()[0] == null ||
            !"submission".equals(dto.getActions()[0].getActionType())
        ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        final Annotatable target = annotatableRepository.findById(annotatableElementId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // check if we see
        if (target.getQuestionnaire() != null && !target.getQuestionnaire().seenBy(username())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // check if we're allowed to annotate
        if (!target.annotatableBy(username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // check if annotation already exists
        // if yes, add as action
        // if no, add annotation and add as action
        final Optional<Annotation> existing = annotationRepository
            .findByTargetIdAndPredicateAndCode(
                target.getId(),
                dto.getPredicate(),
                code.getSystem(),
                code.getSystemVersion(),
                code.getCode());

        Annotation annotation = null;
        if (existing.isEmpty()) {
            annotation = annotationMapper.toEntity(dto);
            // fill object
            annotation.setTarget(target);
            annotation.setStatus(Status.OPEN);
            annotation.getActions().iterator().next().setAuthorName(username());
            annotationRepository.save(annotation);
        } else {
            annotation = existing.get();
            // check if author already submitted same annotation; if true, throw exception
            if (annotation.getActions().stream().anyMatch(action -> action.getAuthorName().equals(username()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }
        }

        final Action action = actionMapper.toEntity(dto.getActions()[0]);
        actionRepository.save(action);
        annotation.addAction(action, target.getQuestionnaire() == null ? null : target.getQuestionnaire().getOptions());
        actionRepository.save(action);
        annotationRepository.save(annotation);

        return annotation.getId();
    }

    /**
     * Delete an annotation and its comments.
     * @param annotationId annotation to remove action from
     * @param actionid action to remove
     */
    public void removeAnnotation(final Long annotationId, final Long actionId) {
        final Annotation annotation = getAnnotation(annotationId);
        final Action action = actionRepository.findById(actionId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        // see if user is allowed to delete
        // -> either this is the user's own annotation or user has admin rights
        if (annotation.removeAction(action, username()) == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        actionRepository.deleteById(actionId);
        annotationRepository.save(annotation);
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
        if (!parent.reviewableBy(username()) && (!parent.hasAuthor(username()) || !Comment.Type.NOTE.equals(dto.getActionType()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN); 
        }

        final Comment comment = actionMapper.toEntity(dto);
        actionRepository.save(comment);

        final QuestionnaireOptions req = parent.getTarget().getQuestionnaire() == null ? null : parent.getTarget().getQuestionnaire().getOptions();
        parent.addAction(comment, req);
        annotationRepository.save(parent);
        actionRepository.save(comment);
        return comment.getId();
    }

    /**
     * Remove a comment.
     * Re-calculates status of the annotation it responds to.
     * @param id Internal ID of the comment to delete
     */
    public void deleteComment(final Long id) {
        final Action action = actionRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!"comment".equals(action.getActionType())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // access control
        final Annotation annotation = action.getAnnotation();
        if (!annotation.seenBy(username())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND); 
        }
        if (!action.getAuthorName().equals(username()) ||
            (annotation.getTarget().getQuestionnaire() != null &&
            annotation.getTarget().getQuestionnaire().administeredBy(username()))
        ) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN); 
        }

        final Comment comment = (Comment) action;
        final Boolean response = annotation.removeAction(comment, username());
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN); 
        } else {
            annotationRepository.save(annotation);
            actionRepository.deleteById(id);
        }
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

        final Set<Annotation> annotations = annotationRepository
            .findAllById(List.of(dto.getAnnotationIds()))
            .stream()
            // we already checked for admin, so we don't need to do this step I guess
            // but when we stream anyways ...
            .filter(a -> a.seenBy(username))
            .collect(Collectors.toSet());
        // check if all annotations exist
        if (annotations.size() != dto.getAnnotationIds().length) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // key must be unique for each questionnaire id
        if (versionRepository.existsByKeyAndQuestionnaireId(dto.getKey(), questionnaireId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        // add versioning actions to annotations to (1) freeze them and (2) notify users
        for (final Annotation annotation : annotations) {
            final Versioning v = Versioning.builder()
                .authorName(username)
                .description(dto.getDescription())
                .versionKey(dto.getKey())
                .build();
            actionRepository.save(v);
            annotation.addAction(v, q.getOptions());
            actionRepository.save(v);
            annotationRepository.save(annotation);
            // TODO: I don't think this is needed
            actionRepository.save(v);
        }

        final Version version = new Version(
            null,
            dto.getKey(),
            dto.getDescription(),
            annotations.toArray(Annotation[]::new),
            q,
            null
        );
        return versionRepository.save(version).getId();
    }

    public VersionDto getVersion(final Long questionnaireId, final String versionKey) {
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
        final Version v = versionRepository.findByKeyAndQuestionnaireId(versionKey, questionnaireId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        dto.setAnnotations(
            Stream.of(v.getAnnotations())
                .map(annotationMapper::toDto)
                .toArray(AnnotationDto[]::new)
        );
        dto.setKey(v.getKey());
        dto.setDescription(v.getDescription());
        dto.setId(v.getId());
        dto.setSubmittedAt(v.getSubmittedAt());
        return dto;
    }

    public String[] getVersionKeys(final Long questionnaireId) {
        final String username = username();
        final Questionnaire q = questionnaireRepository.findById(questionnaireId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // access control
        if (!q.seenBy(username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else if (!q.administeredBy(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return versionRepository.findKeysByQuestionnaireId(questionnaireId).toArray(String[]::new);
    }


    //////////////////////////////
    /// FUN USER-RELATED STUFF ///
    //////////////////////////////

    /**
     * @param username user to get all the annotations of
     * @return all annotations by the given user in the last 7 days
     */
    public AnnotationDto[] getRecentAnnotations(final String username) {
        final var stream = annotationRepository.findAllByActionAuthorNameAndActionActionTypeAndActionSubmittedAtAfter(
                username,
                "submission",
                Instant.now().minus(7, ChronoUnit.DAYS)
            )
            .stream();
        // if we are the queried user, just return all
        // else, filter for annotations we are allowed to edit (because else we don't
        // see their annotaitons!)
        if (!username().equals(username)) {
            stream.filter(annotation -> annotation.reviewableBy(username()));
        }
        return stream
            .map(annotationMapper::toDto)
            .toArray(AnnotationDto[]::new);
    }

    public RichActionDto[] getRecentActions(final String username) {
        final var stream = actionRepository.findByAuthorNameAndSubmittedAtAfter(
                username,
                Instant.now().minus(7, ChronoUnit.DAYS)
            )
            .stream();
        // if we are the queried user, just return all
        // else, filter for annotations we are allowed to edit (because else we don't
        // see their annotaitons!)
        if (!username().equals(username)) {
            stream.filter(action -> action.getAnnotation() != null && action.getAnnotation().reviewableBy(username()));
        }
        return stream
            .map(actionMapper::toRichDto)
            .toArray(RichActionDto[]::new);
    }

    /**
     * @returns username of current user
     */
    private String username() {
        final Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaimAsString("preferred_username");
    }
}
