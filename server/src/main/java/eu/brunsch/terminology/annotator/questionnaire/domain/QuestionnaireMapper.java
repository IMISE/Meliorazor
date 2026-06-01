package eu.brunsch.terminology.annotator.questionnaire.domain;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(
    componentModel = ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface QuestionnaireMapper {
    /**
     * Map questionnaire to its DTO.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    QuestionnaireDto toDto(Questionnaire entity);

    /**
     * Map an incoming questionnaire DTO to questionnaire.
     * @param dto incoming DTO
     * @return entity which can be saved
     */
    Questionnaire toEntity(QuestionnaireDto dto);

}
