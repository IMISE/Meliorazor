package eu.brunsch.terminology.annotator.questionnaire.domain.group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import eu.brunsch.terminology.annotator.questionnaire.domain.Questionnaire;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Groups")
public class Group {
    /** Identifier value. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Display name of the group. */
    @NotBlank
    private String name;

    /** Additional information. */
    @NotBlank
    private String description;

    /** A URL that leads to further information about this group. */
    @Nullable
    private String website;

    /** Assigns usernames permissions. */
    @ElementCollection
    @NotEmpty
    private Participant[] participants;

    /** Invited participants need to accept or reject their invitation. */
    @ElementCollection
    @NotNull
    private Participant[] invitees;

    /** Subordinate groups to support nested organisations. */
    @OneToMany(mappedBy = "parentGroup", fetch = FetchType.LAZY)
    @NotNull
    private Set<Group> subGroups;

    /**
     * FHIR servers that may be queried for CodeSystems/ValueSets.
     * These are inherited to our subgroups, they may add upon them, but (at this
     * time) can't remove any.
     */
    @NotNull
    private Set<String> allowedServers;

    /**
     * Projects created on this group, not its children or parent. Has customized
     * participants and servers, though they're a subset of the ones defined here.
     */
    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER)
    @NotNull
    private Set<Questionnaire> projects;

    /**
     * Superordniate group that hands down its servers and suggests participants for
     * this group.
     */
    @ManyToOne
    @Nullable
    private Group parentGroup;

    public Set<String> getAllowedServers() {
        final Set<String> servers = new HashSet<>(allowedServers);

        Group ancestor = getParentGroup();
        while (ancestor != null) {
            servers.addAll(ancestor.getAllowedServers());
            ancestor = ancestor.getParentGroup();
        }

        return servers;
    }

    public List<Group> getAncestors() {
        final List<Group> ancestors = new ArrayList<>();
        Group group = this;
        while (group != null) {
            ancestors.add(group);
            group = group.getParentGroup();
        }
        return ancestors;
    }

    public GroupDto toDtoSeenBy(String username, GroupMapper mapper) {
        if (!seenBy(username))
            return null;

        final GroupDto dto = mapper.toShallowDto(this);
        final GroupDto[] children = subGroups.stream().filter(g -> g.seenBy(username))
                .map(g -> g.toDtoSeenBy(username, mapper)).toArray(GroupDto[]::new);
        dto.setSubGroups(children);

        return dto;
    }

    /**
     * Whether a user with the given username is able to see this questionnaire.
     * This questionnaire should not be displayed to this user if it answers
     * `false`.
     * 
     * @param username set on questionnaire and taken from claims
     * @return whether user is allowed to see this
     */
    public boolean seenBy(final String username) {
        for (Participant participant : getParticipants()) {
            if (participant.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInvited(final String username) {
        for (Participant participant : getInvitees()) {
            if (participant.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether a user with the given username is able to administer this
     * questionnaire.
     * Editing or deleting (parts of) this project should be rejected if it answers
     * `false`.
     * 
     * @param username set on questionnaire and taken from claims
     * @return whether user is allowed to administer this
     */
    public boolean administeredBy(final String username) {
        for (Participant participant : getParticipants()) {
            if (participant.getUsername().equals(username)
                    && List.of(participant.getRoles()).contains(Participant.Role.ADMIN)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the usernames of users with admin permission for this group.
     * 
     * @return A set of usernames of people with {@link Participant.Role
     *         Role.ADMIN}.
     */
    public List<String> getAdmins() {
        return Stream.of(getParticipants()).map(p -> p.getUsername()).filter(this::administeredBy).toList();
    }

    public Participant getUser(final String username) {
        return Stream.of(getParticipants())
                .filter(p -> p.getUsername().equals(username))
                .findAny()
                .orElseGet(null);
    }

    public Participant getInvitee(final String username) {
        return Stream.of(getInvitees())
                .filter(p -> p.getUsername().equals(username))
                .findAny()
                .orElseGet(null);
    }

    /**
     * Removes a user from this group and all its descendants.
     * 
     * @param username name of participant/invitee to remove
     * @return a list of all changed groups that need to be saved
     */
    public Set<Group> removeUser(final String username) {
        Set<Group> changedGroups = new HashSet<>();

        Participant[] changedInvites = Stream.of(invitees).filter(p -> !p.getUsername().equals(username))
                .toArray(Participant[]::new);
        if (changedInvites.length < getInvitees().length) {
            setParticipants(changedInvites);
            changedGroups.add(this);
        } else {
            Participant[] changedParticipants = Stream.of(invitees).filter(p -> !p.getUsername().equals(username))
                    .toArray(Participant[]::new);
            if (changedParticipants.length < getParticipants().length) {
                setParticipants(changedParticipants);
                changedGroups.add(this);
            }
        }

        if (getSubGroups() != null) {
            for (final Group child : getSubGroups()) {
                changedGroups.addAll(child.removeUser(username));
            }
        }

        return changedGroups;
    }

    public boolean hasParent(final Long groupId) {
        Group ancestor = getParentGroup();
        while (ancestor != null) {
            if (ancestor.getId().equals(groupId)) {
                return true;
            }
        }
        return false;
    }
}
