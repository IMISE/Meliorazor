package eu.brunsch.terminology.annotator.questionnaire.domain.annotation;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;

@Mapper(
    componentModel = ComponentModel.SPRING
)
public abstract class AnnotationMapper {

    /** Needed to map Set<Action> to ActionDto[] and vice versa. */
    @Setter(onMethod_ = @Autowired)
    private ActionMapper actionMapper;

    //
    // actually interesting methods
    //

    /**
     * Map an annotation to its DTO.
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    public abstract AnnotationDto toDto(Annotation entity);

    /**
     * Map an incoming annotation DTO to an annotation.
     * Target must be mapped separately.
     * @param dto incoming DTO
     * @return entity which can be saved
     */
    @Mapping(target = "target", ignore = true)
    public abstract Annotation toEntity(AnnotationDto dto);

    //
    // helpers to map Set<Actions> to ActionDto[] and vice versa
    // (property actions of Annotation)
    //

    /**
     * Map a set of actions to their DTO.
     * @param entities set of to-be-displayed entities
     * @return Array of DTOs which can be displayed to user
     */
    public ActionDto[] toDtos(Set<Action> entities) {
        return entities.stream()
            .sorted((a1, a2) -> a1.getSubmittedAt().compareTo(a2.getSubmittedAt()))
            .map(actionMapper::toDto)
            .toArray(ActionDto[]::new);
    }

    /**
     * Map an incoming action DTO to an action.
     * @param dto incoming DTO
     * @return entity which can be saved
     */
    public Set<Action> toEntities(ActionDto[] dtos) {
        return Stream.of(dtos).map(actionMapper::toEntity).collect(Collectors.toSet());
    }

}
