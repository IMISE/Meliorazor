package eu.brunsch.terminology.annotator.questionnaire.domain;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(
    componentModel = ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ChoiceMapper {
    /**
     * Map choice to its DTO.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    ChoiceDto toDto(Choice entity);

    /**
     * Map an incoming choice DTO to choice.
     * @param dto incoming DTO
     * @return entity which can be saved
     */
    Choice toEntity(ChoiceDto dto);

}
