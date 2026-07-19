package eu.brunsch.terminology.annotator.questionnaire.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.brunsch.terminology.annotator.questionnaire.domain.QuestionnaireDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.VersionCreateDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.VersionDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.DataElementDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.RichActionDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.AnnotationDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.CommentDto;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
public class AnnotationController {

    /** Business stuff! */
    private final AnnotationService service;

    /**
     * Get all accessible questionnaires.
     * Does not include data elements, annotations, comments.
     * @return all questionnaires the user has access to
     */
    @GetMapping("questionnaire")
    public QuestionnaireDto[] getQuestionnaires() {
        return service.getAllQuestionnaires();
    }

    /**
     * Import a questionnaire.
     * No possibility to add data elements later.
     * @param dto information about the questionnaire
     * @return ID of the created questionnaire.
     */
    @PostMapping("questionnaire")
    public Long importQuestionnaire(final @RequestBody QuestionnaireDto dto) {
        return service.importQuestionnaire(dto);
    }

    /**
     * Delete a questionnaire.
     * Preserves data elements and annotations for future use.
     * @param id identifier of the questionnaire
     */
    @DeleteMapping("questionnaire/{id}")
    public void deleteQuestionnaire(final @PathVariable Long id) {
        service.deleteQuestionnaire(id);
    }

    /**
     * Get a single questionnaire.
     * @param id identifier of the questionnaire
     * @return questionnaire
     */
    @GetMapping("questionnaire/{id}")
    public QuestionnaireDto getQuestionnaire(final @PathVariable Long id) {
        return service.getQuestionnaire(id);
    }

    /**
     * Edit a single questionnaire.
     * @param id identifier of the questionnaire
     * @param dto new contents; ignores ID, data elements and empty (undefined/null) properties.
     */
    @PutMapping("questionnaire/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editQuestionnaire(final @PathVariable Long id, final @RequestBody QuestionnaireDto dto) {
        service.editQuestionnaire(id, dto);
    }


    /**
     * Get a whole data element, incl. choices, units and all annotations and actions.
     * @param id identifier of the data element
     * @return data element
     */
    @GetMapping("dataElement/{id}")
    public DataElementDto getDataElement(final @PathVariable Long id) {
        return service.getDataElementWithComments(id);
    }

    /**
     * Get the annotations with comments of a data element.
     * @param id identifier of the data element
     * @return only annotations of the data element (of any status)
     */
    @GetMapping("dataElement/{id}/annotation")
    public AnnotationDto[] getAnnotations(final @PathVariable Long id) {
        return service.getDataElementsAnnotations(id);
    }

    /**
     * Get a specific annotation.
     * @param id identifier of the annotation
     * @return annotation with comments
     */
    @GetMapping("annotatable/{dataElement}/annotation/{id}")
    public AnnotationDto getAnnotation(final @PathVariable Long id) {
        return service.getAnnotationDto(id);
    }


    /**
     * Annotate an annotatable element.
     * @param annotatableId identifier of the annotatable element.
     * @param dto information about the annotation
     * @return ID of the newly created annotation
     */
    @PostMapping("annotatable/{annotatableId}/annotation")
    public Long createAnnotation(
        final @PathVariable Long annotatableId,
        final @RequestBody AnnotationDto dto
    ) {
        return service.createAnnotation(annotatableId, dto);
    }

    /**
     * Remove an annotation.
     * Removes associated comments as well.
     * @param id identifier of the annotation
     */
    @DeleteMapping("dataElement/{dataElement}/annotation/{annotationId}/action/{actionId}")
    public void deleteAnnotation(final @PathVariable Long annotationId, final @PathVariable Long actionId) {
        service.removeAnnotation(annotationId, actionId);
    }

    /**
     * Comment on an annotation.
     * May change the status of the annotation.
     * @param id identifier of the annotation
     * @param dto information about the comment
     * @return ID of the newly created comment
     */
    @PostMapping("dataElement/{dataElement}/annotation/{id}/comment")
    public Long createComment(
        final @PathVariable Long id,
        final @RequestBody CommentDto dto
    ) {
        return service.addComment(id, dto);
    }

    /**
     * Remove a comment.
     * May change the status of the annotation.
     * @param id identifier of the comment
     */
    @DeleteMapping("annotatable/{a}/annotation/{b}/comment/{id}")
    public void deleteComment(final @PathVariable Long id) {
        service.deleteComment(id);
    }

    /**
     * Group together annotations in a version.
     * @param questionnaireId Questionnaire that's getting a new version.
     * @param versionDto Annotation IDs, version name and short description or other.
     * @return
     */
    @PostMapping("questionnaire/{questionnaireId}/version")
    public Long createVersion(
        final @PathVariable Long questionnaireId,
        final @RequestBody VersionCreateDto versionDto
    ) {
        return service.createVersion(questionnaireId, versionDto);
    }

    @GetMapping("questionnaire/{questionnaireId}/version")
    public String[] getVersionKeys(
        final @PathVariable Long questionnaireId
    ) {
        return service.getVersionKeys(questionnaireId);
    }

    @GetMapping("questionnaire/{questionnaireId}/version/{versionKey}")
    public VersionDto getVersion(
        final @PathVariable Long questionnaireId,
        final @PathVariable String versionKey
    ) {
        return service.getVersion(questionnaireId, versionKey);
    }

    @GetMapping("user/{username}/annotations/recent")
    public AnnotationDto[] getRecentAnnotations(final @PathVariable String username) {
        return service.getRecentAnnotations(username);
    }

    @GetMapping("user/{username}/actions/recent")
    public RichActionDto[] getRecentActions(final @PathVariable String username) {
        return service.getRecentActions(username);
    }

}
