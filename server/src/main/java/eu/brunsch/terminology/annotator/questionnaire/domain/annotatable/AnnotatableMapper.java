package eu.brunsch.terminology.annotator.questionnaire.domain.annotatable;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING)
public abstract class AnnotatableMapper implements Annotatable.Visitor, AnnotatableDto.Visitor {
    //
    // General Entity <-> General DTO
    // Only useful for Annotation actions I think
    //

    /**
     * Map an action to its DTO.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    public AnnotatableDto toDto(Annotatable entity) {
        return entity.accept(this);
    }

    /**
     * Map an incoming action DTO to an action.
     * @param dto incoming DTO
     * @return entity which can be saved
     */
    public Annotatable toEntity(AnnotatableDto dto) {
        return dto.accept(this);
    }

    //
    // DTO => ENTITY
    //

    /**
     * Map a choice to its DTO.
     * WITHOUT ANNOTATIONS.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    @Mapping(target = "annotations", ignore = true)
    public abstract ChoiceDto toDto(Choice entity);

    /**
     * Map a datae lement to its DTO.
     * WITHOUT ANNOTATIONS.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    @Mapping(target = "annotations", ignore = true)
    public abstract DataElementDto toDto(DataElement entity);

    /**
     * Map a unit to its DTO.
     * WITHOUT ANNOTATIONS.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    @Mapping(target = "annotations", ignore = true)
    public abstract UnitDto toDto(Unit entity);

    //
    // ENTITY => DTO
    //

    /**
     * Map an incoming choice DTO to a choice.
     * @param dto incoming DTO
     * @return entity which can be saved
     */
    public abstract Choice toEntity(ChoiceDto dto);

    /**
     * Map an incoming data element DTO to a data element.
     * @param dto incoming DTO
     * @return entity which can be saved, after populating questionnaire
     */
    @Mapping(target = "questionnaire", ignore = true)
    public abstract DataElement toEntity(DataElementDto dto);

    /**
     * Map an incoming unit DTO to a unit.
     * Variable needs to be populated manually.
     * @param dto incoming DTO
     * @return entity without variable, can be saved after that's populated
     */
    @Mapping(target = "variable", ignore = true)
    public abstract Unit toEntity(UnitDto dto);

    //
    // implementation of interfaces
    //

    @Override
    public AnnotatableDto visit(Choice choice) {
        return toDto(choice);
    }

    @Override
    public AnnotatableDto visit(DataElement dataElement) {
        return toDto(dataElement);
    }

    @Override
    public AnnotatableDto visit(Unit unit) {
        return toDto(unit);
    }


    @Override
    public Annotatable visit(ChoiceDto choice) {
        return toEntity(choice);
    }

    @Override
    public Annotatable visit(DataElementDto dataElement) {
        return toEntity(dataElement);
    }

    @Override
    public Annotatable visit(UnitDto unit) {
        return toEntity(unit);
    }

}
