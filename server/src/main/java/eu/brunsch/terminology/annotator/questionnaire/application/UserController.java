package eu.brunsch.terminology.annotator.questionnaire.application;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.RichActionDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.AnnotationDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.group.GroupInvitationDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(description = "Endpoints for interacting with users.", name = "Users")
public class UserController {
    /** Business stuff! */
    private final UserService service;

    @GetMapping("{username}/annotations/recent")
    public AnnotationDto[] getRecentAnnotations(final @PathVariable String username) {
        return service.getRecentAnnotations(username);
    }

    @GetMapping("{username}/actions/recent")
    public RichActionDto[] getRecentActions(final @PathVariable String username) {
        return service.getRecentActions(username);
    }

    @GetMapping("{username}/invitations")
    public GroupInvitationDto[] getInvitations(final @PathVariable String username) {
        return service.getGroupInvitations(username);
    }
}
