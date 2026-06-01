package eu.brunsch.terminology.annotator.questionnaire.domain;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(
    componentModel = ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AnnotationMapper {
    /**
     * Map an annotation to its DTO.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    AnnotationDto toDto(Annotation entity);

    /**
     * Map an incoming annotation DTO to an annotation.
     * @param dto incoming DTO
     * @return entity which can be saved
     */
    Annotation toEntity(AnnotationDto dto);
}
