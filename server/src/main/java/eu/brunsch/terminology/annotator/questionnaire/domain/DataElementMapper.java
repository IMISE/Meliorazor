package eu.brunsch.terminology.annotator.questionnaire.domain;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(
    componentModel = ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DataElementMapper {
    /**
     * Map data element to its DTO.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    DataElementDto toDto(DataElement entity);

    /**
     * Map data element to its DTO, WITHOUT choices.
     * For the case where we need to add annotations to choice DTOs.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user WITHOUT choices
     */
    @Mapping(target = "choices", ignore = true)
    DataElementDto toDtoWithutChoices(DataElement entity);

    /**
     * Map an incoming data element DTO to data element.
     * @param dto incoming DTO
     * @return entity which can be saved, EXCEPT choices! you need to do the choices seperately
     */
    @Mapping(target = "choices", ignore = true)
    DataElement toEntity(DataElementDto dto);

}
