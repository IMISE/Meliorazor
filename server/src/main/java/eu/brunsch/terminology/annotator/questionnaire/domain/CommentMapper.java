package eu.brunsch.terminology.annotator.questionnaire.domain;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(
    componentModel = ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CommentMapper {
    /**
     * Map a comment to its DTO.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    CommentDto toDto(Comment entity);

    /**
     * Map an incoming comment DTO to a comment.
     * @param dto incoming DTO
     * @return entity which can be saved
     */
    Comment toEntity(CommentDto dto);
}
