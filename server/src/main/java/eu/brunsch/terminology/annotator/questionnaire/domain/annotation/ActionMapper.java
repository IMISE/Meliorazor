package eu.brunsch.terminology.annotator.questionnaire.domain.annotation;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

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
    // DTO => ENTITY
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

    //
    // ENTITY => DTO
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
