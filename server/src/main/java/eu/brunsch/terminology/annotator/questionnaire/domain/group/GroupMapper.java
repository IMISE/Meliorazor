package eu.brunsch.terminology.annotator.questionnaire.domain.group;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

import eu.brunsch.terminology.annotator.utils.DoIgnore;

@Mapper(componentModel = ComponentModel.SPRING)
public interface GroupMapper {
    /**
     * Map group to its DTO.
     * 
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user
     */
    public GroupDto toDto(Group entity);

    /**
     * Map an incoming group DTO to group.
     * 
     * @param dto incoming DTO
     * @return entity which can be saved
     */
    public Group toEntity(GroupDto dto);

     /**
     * Map group to its DTO, ignoring subgroups.
     * 
     * @param entity to-be-displayed entity
     * @return DTO which can be displayed to user, not including subgroups.
     */
    @Mapping(ignore = true, target = "subGroups")
    @DoIgnore
    public GroupDto toShallowDto(Group entity);
}
