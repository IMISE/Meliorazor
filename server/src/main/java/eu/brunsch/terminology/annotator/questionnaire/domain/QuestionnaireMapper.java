package eu.brunsch.terminology.annotator.questionnaire.domain;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(
    componentModel = ComponentModel.SPRING
)
public interface QuestionnaireMapper {
    /**
     * Map questionnaire to its DTO.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    public QuestionnaireDto toDto(Questionnaire entity);

    /**
     * Map an incoming questionnaire DTO to questionnaire.
     * @param dto incoming DTO
     * @return entity which can be saved
     */
    public Questionnaire toEntity(QuestionnaireDto dto);

    /**
     * Map an incoming questionnaire create DTO to questionnaire.
     * @param dto incoming DTO
     * @return entity which can be saved
     */
    public Questionnaire toEntity(QuestionnaireCreateDto dto);

}
