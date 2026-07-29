package eu.brunsch.terminology.annotator.questionnaire.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import eu.brunsch.terminology.annotator.questionnaire.domain.group.Group;
import eu.brunsch.terminology.annotator.questionnaire.domain.group.GroupCreateDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.group.GroupDto;
import eu.brunsch.terminology.annotator.questionnaire.domain.group.GroupMapper;
import eu.brunsch.terminology.annotator.questionnaire.domain.group.Participant;
import eu.brunsch.terminology.annotator.questionnaire.infrastructure.GroupRepository;
import eu.brunsch.terminology.annotator.utils.Utils;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupService {

    /** Group DB access. */
    private final GroupRepository groupRepository;

    /** Map Group DTOs to Group entities (and vice versa). */
    private final GroupMapper groupMapper;

    @Transactional(readOnly = true)
    public GroupDto[] getGroups() {
        final String username = Utils.username();
        final List<Group> groups = groupRepository.findAllByParticipantsUsernameAndParentGroupIsNull(username);
        return groups.stream().map(g -> g.toDtoSeenBy(username, groupMapper)).toArray(GroupDto[]::new);
    }

    @Transactional(readOnly = true)
    private Group getGroup(final Long groupId) {
        final String username = Utils.username();

        final Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!group.seenBy(username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return group;
    }

    @Transactional(readOnly = true)
    public GroupDto getGroupDto(final Long groupId) {
        return getGroup(groupId).toDtoSeenBy(Utils.username(), groupMapper);
    }

    public GroupDto[] getGroupDtosByRoleFlat(final Participant.Role role) {
        final String username = Utils.username();
        return groupRepository
                .findAllByParticipantsUsername(username)
                .stream()
                .filter(g -> g.administeredBy(username))
                .map(groupMapper::toShallowDto).toArray(GroupDto[]::new);
    }

    public Long createGroup(final GroupCreateDto dto) {
        final String username = Utils.username();

        // save this later
        final Group group = new Group();

        // check incoming participants
        List<String> inheritedAdmins = new ArrayList<>();
        inheritedAdmins.add(username);

        if (dto.getParentGroupId() != null) {
            final Group parentGroup = groupRepository.findById(dto.getParentGroupId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            if (!parentGroup.seenBy(username)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            if (!parentGroup.administeredBy(username)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            inheritedAdmins = parentGroup.getAdmins();
            group.setParentGroup(parentGroup);
        }

        // array must exist, not be empty and values must exist and be valid
        // admins must be present
        if (!Participant.checkConsistent(dto.getParticipants(), username, inheritedAdmins)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (!Utils.isUri(dto.getWebsite())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setWebsite(dto.getWebsite());
        group.setAllowedServers(new HashSet<>());
        group.setProjects(new HashSet<>());
        group.setSubGroups(new HashSet<>());

        // inherited admins are added, not invited
        final List<Participant> participants = new ArrayList<>();
        // everyone else must accept their invitation
        final List<Participant> invitees = new ArrayList<>();
        for (final Participant p : dto.getParticipants()) {

            // everyone should have INFO role
            Participant newUser = p;
            if (!p.hasRole(Participant.Role.INFO)) {
                final Participant.Role[] roles = Arrays.copyOf(p.getRoles(), p.getRoles().length + 1);
                roles[roles.length - 1] = Participant.Role.INFO;
            }

            if (inheritedAdmins.contains(p.getUsername())) {
                participants.add(newUser);
            } else {
                invitees.add(newUser);
            }
        }
        group.setParticipants(participants.toArray(Participant[]::new));
        group.setInvitees(invitees.toArray(Participant[]::new));

        return groupRepository.save(group).getId();
    }

    public void setParticipants(final Long groupId, final Participant[] participants) {
        final String username = Utils.username();

        // check if request is ok
        if (participants == null || participants.length < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        final Group group = getGroup(groupId);
        if (!group.administeredBy(Utils.username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        final Group parent = group.getParentGroup();

        final Set<String> requiredAdmins = new HashSet<>();
        requiredAdmins.add(username);
        if (parent != null) {
            requiredAdmins.addAll(parent.getAdmins());
        }
        if (!Participant.checkConsistent(participants, group.getParticipants()[0].getUsername(), requiredAdmins)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // find and process newly invited and other (un)changed users
        List<Participant> newParticipants = new ArrayList<>();
        List<Participant> newInvites = new ArrayList<>();
        for (final Participant _change : participants) {
            Participant change = _change;
            // leaving INFO out is not an error, but it should still be present
            if (!change.hasRole(Participant.Role.INFO)) {
                final Participant.Role[] roles = Arrays.copyOf(change.getRoles(), change.getRoles().length + 1);
                roles[roles.length - 1] = Participant.Role.INFO;
            }

            final Participant invitee = group.getInvitee(change.getUsername());
            if (invitee != null) {
                // we invited this person previously
                // no further action required
                newInvites.add(change);
            } else {
                final Participant user = group.getUser(change.getUsername());
                if (user != null) {
                    // this person already accepted their invitation
                    // no further action required
                    newParticipants.add(change);
                } else {
                    // newly invited user
                    // add to this project and all parent project where they aren't present
                    Group ancestor = parent;
                    while (ancestor != null && !ancestor.seenBy(change.getUsername())) {
                        Participant[] ancestorInvites = Arrays.copyOf(ancestor.getInvitees(),
                                ancestor.getInvitees().length + 1);
                        ancestorInvites[ancestorInvites.length - 1] = change;
                        ancestor.setInvitees(ancestorInvites);
                        groupRepository.save(ancestor);
                        ancestor = ancestor.getParentGroup();
                    }
                    newInvites.add(change);
                }
            }

            // find and process removed users
            if (group.getParticipants().length < newParticipants.size()) {
                Stream.of(group.getParticipants()).filter(
                        p1 -> !newParticipants.stream().anyMatch(p2 -> p1.getUsername().equals(p2.getUsername())))
                        .forEach(p -> {
                            groupRepository.saveAll(group.removeUser(p.getUsername()));
                        });
            }
            if (group.getInvitees().length < newInvites.size()) {
                Stream.of(group.getInvitees())
                        .filter(p1 -> !newInvites.stream().anyMatch(p2 -> p1.getUsername().equals(p2.getUsername())))
                        .forEach(p -> {
                            groupRepository.saveAll(group.removeUser(p.getUsername()));
                        });

            }

            group.setInvitees(newInvites.toArray(Participant[]::new));
            group.setParticipants(newParticipants.toArray(Participant[]::new));
            groupRepository.save(group);
        }
    }

    public void setServers(final Long groupId, final Set<String> allowedServers) {
        if (allowedServers != null && allowedServers.size() > 0 && !allowedServers.contains(null)
                && allowedServers.stream().allMatch(s -> Utils.isUri(s))) {
            final Group group = getGroup(groupId);
            if (!group.administeredBy(Utils.username())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            group.setAllowedServers(allowedServers);
            groupRepository.save(group);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    public void acceptInvitation(final Long groupId) {
        final Group group = getGroup(groupId);
        final String username = Utils.username();
        if (!group.isInvited(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Group g = group;
        while (g != null) {
            Participant participant = g.getInvitee(username);
            if (participant == null) {
                return;
            } else {
                Participant[] newInvitees = Stream.of(g.getInvitees()).filter(p -> !participant.equals(p))
                        .toArray(Participant[]::new);
                Participant[] newParticipants = Arrays.copyOf(g.getParticipants(), g.getParticipants().length + 1);
                newParticipants[newParticipants.length - 1] = participant;

                g.setInvitees(newInvitees);
                g.setParticipants(newParticipants);

                groupRepository.save(g);
            }
        }
    }

    public void declineInvitation(final Long groupId) {
        final Group group = getGroup(groupId);
        final String username = Utils.username();
        if (group.isInvited(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        group.removeUser(username).stream().forEach(groupRepository::save);
    }

}
