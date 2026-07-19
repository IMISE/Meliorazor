package eu.brunsch.terminology.annotator.questionnaire.domain.annotation;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.Choice;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.DataElement;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.RichActionDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.Unit;

@Mapper(componentModel = ComponentModel.SPRING)
public abstract class ActionMapper implements Action.Visitor, ActionDto.Visitor {
    //
    // actually interesting methods
    //

    /**
     * Map an action to its DTO.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    public ActionDto toDto(Action entity) {
        return entity.accept(this);
    }

    /**
     * Map an incoming action DTO to an action.
     * @param dto incoming DTO
     * @return entity which can be saved
     */
    public Action toEntity(ActionDto dto) {
        return dto.accept(this);
    }

    //
    // ENTITY => DTO
    //

    /**
     * Map a comment to its DTO.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    public abstract CommentDto toDto(Comment entity);

    /**
     * Map a submission to its DTO.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    public abstract SubmissionDto toDto(Submission entity);

    /**
     * Map a versioning to its DTO.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    public abstract VersioningDto toDto(Versioning entity);

    public RichActionDto toRichDto(Action action) {
        final var target = action.getAnnotation().getTarget();
        final var questionnaire = target.getQuestionnaire();

        Long dataElementId = null;
        String dataElementLabel = null;

        if (target instanceof DataElement) {
            dataElementId = target.getId();
            dataElementLabel = target.getLabel();
        } else if (target instanceof Choice) {
            final DataElement variable = ((Choice) target).getVariable();
            dataElementId = variable.getId();
            dataElementLabel = variable.getLabel();
        } else if (target instanceof Unit) {
            final DataElement variable = ((Unit) target).getVariable();
            dataElementId = variable.getId();
            dataElementLabel = variable.getLabel();
        }

        return new RichActionDto(
            toDto(action),
            questionnaire == null ? null : questionnaire.getId(),
            questionnaire == null ? null : questionnaire.getTitle(),
            dataElementId,
            dataElementLabel,
            action.getAnnotation().getId(),
            action.getAnnotation().getPredicate() + " " + action.getAnnotation().getCode().getDisplay(),
            target.getId(),
            target.getLabel()
        );
    }

    //
    // DTO => ENTITY
    //

    /**
     * Map an incoming comment DTO to a comment.
     * @param dto incoming DTO
     * @return entity which can be saved
     */
    public abstract Comment toEntity(CommentDto dto);

    /**
     * Map an incoming submission DTO to a submission.
     * @param dto incoming DTO
     * @return entity which can be saved
     */
    public abstract Submission toEntity(SubmissionDto dto);

    /**
     * Map an incoming versioning DTO to a versioning.
     * @param dto incoming DTO
     * @return entity which can be saved
     */
    public abstract Versioning toEntity(VersioningDto dto);

    //
    // implementation of interfaces
    //

    @Override
    public ActionDto visit(Comment comment) {
        return toDto(comment);
    }

    @Override
    public ActionDto visit(Submission submission) {
        return toDto(submission);
    }

    @Override
    public ActionDto visit(Versioning versioning) {
        return toDto(versioning);
    }


    @Override
    public Action visit(CommentDto comment) {
        return toEntity(comment);
    }

    @Override
    public Action visit(SubmissionDto submission) {
        return toEntity(submission);
    }

    @Override
    public Action visit(VersioningDto versioning) {
        return toEntity(versioning);
    }

}
