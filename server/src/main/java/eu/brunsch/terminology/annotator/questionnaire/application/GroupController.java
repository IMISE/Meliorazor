package eu.brunsch.terminology.annotator.questionnaire.application;

import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.brunsch.terminology.annotator.questionnaire.domain.group.GroupCreateDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.group.GroupDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.group.Participant;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/groups")
@Tag(description = "Endpoints for interacting with groups.", name = "Groups")
public class GroupController {
    /** Business stuff! */
    private final GroupService service;

    @PostMapping("")
    public Long createGroup(final @RequestBody GroupCreateDto group) {
        return service.createGroup(group);
    }

    @GetMapping("")
    public GroupDto[] getGroups() {
        return service.getGroups();
    }

    @GetMapping("{groupId}")
    public GroupDto getGroup(final @PathVariable Long groupId) {
        return service.getGroupDto(groupId);
    }

    @PutMapping("{groupId}/participants")
    public void setParticipants(final @PathVariable Long groupId, final @RequestBody Participant[] participants) {
        service.setParticipants(groupId, participants);
    }

    @PutMapping("{groupId}/servers")
    public void setServers(final @PathVariable Long groupId, final @RequestBody String[] servers) {
        service.setServers(groupId, Set.of(servers));
    }

    @PostMapping("{groupId}/accept")
    public void acceptInvitation(final @PathVariable Long groupId) {
        service.acceptInvitation(groupId);
    }

    @PostMapping("{groupId}/decline")
    public void declineInvitation(final @PathVariable Long groupId) {
        service.declineInvitation(groupId);
    }

}
