package eu.brunsch.terminology.annotator.questionnaire.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.RichActionDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.ActionMapper;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.AnnotationDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.AnnotationMapper;
import eu.brunsch.terminology.annotator.questionnaire.domain.group.Group;
import eu.brunsch.terminology.annotator.questionnaire.domain.group.GroupInvitationDto;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.ActionRepository;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.AnnotationRepository;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.GroupRepository;
import eu.brunsch.terminology.annotator.utils.Utils;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    /** Annotation DB access. */
    private final AnnotationRepository annotationRepository;
    /** Annotation Action DB access. */
    private final ActionRepository actionRepository;

    /** Map Annotation DTOs to Annotation entities (and vice versa). */
    private final AnnotationMapper annotationMapper;
    /**
     * Map Annotation Action DTOs to Annotation ACtion entities (and vice versa).
     */
    private final ActionMapper actionMapper;
    /** Group DB access. */
    private final GroupRepository groupRepository;

    /**
     * @param username user to get all the annotations of
     * @return all annotations by the given user in the last 7 days
     */
    public AnnotationDto[] getRecentAnnotations(final String username) {
        final String myName = Utils.username();

        final var stream = annotationRepository
                .findAllByActionsAuthorNameAndActionsActionTypeAndActionsSubmittedAtAfter(
                        username,
                        "submission",
                        Instant.now().minus(7, ChronoUnit.DAYS))
                .stream();
        // if we are the queried user, just return all
        // else, filter for annotations we are allowed to edit (because else we don't
        // see their annotaitons!)
        if (!myName.equals(username)) {
            stream.filter(annotation -> annotation.reviewableBy(myName));
        }
        return stream
                .map(annotationMapper::toDto)
                .toArray(AnnotationDto[]::new);
    }

    public RichActionDto[] getRecentActions(final String username) {
        final String myName = Utils.username();

        final var stream = actionRepository.findByAuthorNameAndSubmittedAtAfter(
                username,
                Instant.now().minus(7, ChronoUnit.DAYS))
                .stream();
        // if we are the queried user, just return all
        // else, filter for annotations we are allowed to edit (because else we don't
        // see their annotaitons!)
        if (!myName.equals(username)) {
            stream.filter(action -> action.getAnnotation() != null
                    && action.getAnnotation().reviewableBy(myName));
        }
        return stream
                .map(actionMapper::toRichDto)
                .toArray(RichActionDto[]::new);
    }

    @SuppressWarnings("null")
    public GroupInvitationDto[] getGroupInvitations(final String username) {
        final List<Group> invites = groupRepository.findAllByInviteesUsername(username);

        return invites.stream()
                // this "deduplicates", but I don't think we want that (think about rejecting
                // invites)
                // .filter(group -> !invites.stream().anyMatch(g -> g.hasParent(group.getId())))
                .map(invite -> new GroupInvitationDto(invite.getId(),
                        invite.getAncestors().stream().map(Group::getName).toList().reversed()
                                .toArray(String[]::new),
                        invite.getDescription(), invite.getWebsite(),
                        invite.getInvitee(username).getRoles()))
                .sorted((a, b) -> String.join("|", a.getNames())
                        .compareTo(String.join("|", b.getNames())))
                .toArray(GroupInvitationDto[]::new);
    }

}
