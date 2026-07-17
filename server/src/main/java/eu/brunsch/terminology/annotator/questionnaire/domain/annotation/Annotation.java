package eu.brunsch.terminology.annotator.questionnaire.domain.annotation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.brunsch.terminology.annotator.questionnaire.domain.Participant;
import eu.brunsch.terminology.annotator.questionnaire.domain.Questionnaire;
import eu.brunsch.terminology.annotator.questionnaire.domain.QuestionnaireOptions;
import eu.brunsch.terminology.annotator.questionnaire.domain.Participant.Role;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.Annotatable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Suggests mapping a data element to a terminology code.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UniqueTriple", columnNames = { "predicate", "code", "target_id" }))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Annotation {

    /** Identifier value. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Predicate of mapping triple, e.g. skos:exactMatch */
    @Column(name = "predicate")
    private String predicate;

    /** Terminology code mapped in this annotation. */
    @Embedded
    private Code code;

    /**
     * Reviews and notes on this annotation.
     * These directly imply the status of this annotation.
     */
    @OneToMany(mappedBy = "annotation", cascade = CascadeType.ALL)
    private Set<Action> actions;

    /** Annotated variable. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id")
    private Annotatable target;

    /**
     * Reflects the current acceptance status of this annotation.
     * Updated on comment submission.
     */
    private Status status;

    /** Valid acceptance states. */
    public enum Status {
        /**
         * Annotation is accepted and found in export.
         * May be suggested as valid in future annotation.
         */
        ACCEPTED,
        /**
         * Annotation is rejected and not displayed anymore.
         * May be used to enrich search results in the future.
         */
        REJECTED,
        /**
         * Not enough responses yet to mark es either accepted or rejected.
         */
        OPEN
    }

    /**
     * Get actions visible to the user.
     * 
     * @param username
     * @return if the user has VIEW or FEEDBACK permission: everything; ANNOTATE
     *         permission: everything except submissions by others
     */
    public Set<Action> getActions(final String username) {
        final var participant = Stream.of(target.getQuestionnaire().getParticipants()).findFirst();
        if (participant.isEmpty()) {
            return new HashSet<>();
        }

        final var roles = participant.get().getRoles();
        if (Stream.of(roles).anyMatch(role -> Role.VIEW.equals(role) || Role.FEEDBACK.equals(role))) {
            // users see everything
            return getActions();
        } else if (Stream.of(roles).anyMatch(role -> Role.ANNOTATE.equals(role))) {
            // users see their own submissions
            return getActions()
                    .stream()
                    .filter(action -> !"submission".equals(action.getActionType()) ||
                            action.getAuthorName().equals(username))
                    .collect(Collectors.toSet());
        } else {
            // user has no permission to view anything
            return new HashSet<>();
        }
    }

    /**
     * Add a new comment and update the status of this annotation,
     * if the votes change.
     *
     * @param comment the comment to add
     * @param req     conditions for marking the annotation as accepted or rejected
     * @return whether the status of this annotation changed
     */
    public boolean addAction(
            final Action action,
            final QuestionnaireOptions req) {
        if (actions == null) {
            actions = new HashSet<Action>();
        }

        actions.add(action);
        action.setAnnotation(this);

        if ("comment".equals(action.getActionType())) {
            Comment comment = (Comment) action;
            return this.updateStatus(comment.getCommentType(), req);
        } else {
            return false;
        }
    }

    /**
     * Remove a comment and update the status of this annotation (if required).
     *
     * @param comment  the comment to remove
     * @param username username of the person trying to edit these submissions
     * @return whether the status of this annotation changed; null if deletion is
     *         impossible
     */
    public Boolean removeAction(
            final Action action,
            final String username) {
        if (actions == null) {
            actions = new HashSet<Action>();
        } else if (actions.stream().anyMatch(a -> "versioning".equals(a.getActionType()))) {
            // can't delete after versioning at least once
            return null;
        } else if (!action.getAuthorName().equals(username) && (getTarget().getQuestionnaire() == null
                || !getTarget().getQuestionnaire().administeredBy(username))) {
            // can't delete when neither authoring change nor administrating project
            return null;
        } else {
            actions.remove(action);
        }

        final QuestionnaireOptions req = getTarget().getQuestionnaire() == null
                ? getTarget().getQuestionnaire().getOptions()
                : null;
        if ("comment".equals(action.getActionType())) {
            final Comment comment = (Comment) action;
            return this.updateStatus(comment.getCommentType(), req);
        } else {
            return false;
        }
    }

    /**
     * Update the status of this annotation by re-reading the comments.
     * Will skipped if type of the changed comment is Comment.Type.NOTE;
     * if no comment changed, just pass Comment.Type.ACCEPT.
     *
     * @param changedType type of the added/changed/removed comment
     * @param req         conditions for marking the annotation as accepted or
     *                    rejected
     * @return whether the status changed
     */
    private boolean updateStatus(
            final Comment.Type changedType,
            final QuestionnaireOptions req) {
        // find out the new status of this annotation
        boolean statusChanged = false;
        if (changedType != Comment.Type.NOTE) {
            final var relevant = this.getRelevantVotes();
            final int total = relevant.size();
            final int accept = (int) relevant
                    .stream()
                    .filter(c -> c.getCommentType() == Comment.Type.ACCEPT)
                    .count();
            final int reject = total - accept;

            final Status newStatus = req.checkAccepted(accept, total)
                    ? Status.ACCEPTED
                    : req.checkRejected(reject, total)
                            ? Status.REJECTED
                            : Status.OPEN;
            if (newStatus != status) {
                statusChanged = true;
                setStatus(newStatus);
            }
        }

        return statusChanged;
    }

    /**
     * Get comments (of types accept/reject),
     * but only the newest one from each user.
     *
     * @return comments which count for acceptance/rejection classification
     */
    @SuppressWarnings("null")
    private Collection<Comment> getRelevantVotes() {
        return actions.stream()
                .filter(c -> c != null && "comment".equals(c.getActionType()))
                .map(c -> (Comment) c)
                // notes can never change status
                .filter(c -> c.getCommentType() == Comment.Type.NOTE)
                // only newest comment of each author counts
                .collect(
                        Collectors.toMap(
                                Comment::getAuthorName,
                                (c) -> c,
                                (c1, c2) -> c1
                                        .getSubmittedAt()
                                        .isAfter(c2.getSubmittedAt()) ? c1 : c2))
                .values();
    }

    /**
     * Did a user with this username submit this annotation?
     * 
     * @param username username of user sending the request
     * @return whether any action of type submission was authored by username
     */
    public boolean hasAuthor(final String username) {
        return getActions() == null
                ? false
                : getActions()
                        .stream()
                        .anyMatch(action -> action.getAuthorName().equals(username) &&
                                "submission".equals(action.getActionType()));
    }

    /**
     * Whether a user with the given username is able to see this annotation.
     * This annotation should not be displayed to this user if it answers `false`.
     * 
     * @param username set on annotation and questionnaire and taken from claims
     * @return whether user is allowed to see this
     */
    public boolean seenBy(final String username) {
        final Questionnaire q = getTarget().getQuestionnaire();
        if (q == null) {
            return true;
        }

        // participants with EDIT and VIEW permission can see this annotation
        for (Participant participant : q.getParticipants()) {
            if (participant.getUsername().equals(username)) {
                final List<Participant.Role> roles = List.of(participant.getRoles());
                if (roles.contains(Participant.Role.FEEDBACK) || roles.contains(Participant.Role.VIEW)) {
                    return true;
                }
            }
        }

        // authors can see this annotation if they submitted something similiar
        for (Action action : getActions()) {
            if (action.getActionType() == "submission" && action.getAuthorName().equals(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether a user with the given username is able to review annotations on data
     * elements of this questionnaire.
     * Feedback by this user on data elements of this project should be rejected if
     * it answers `false`.
     * 
     * @param username set on annotation and questionnaire and taken from claims
     * @return whether user is allowed to give feedback on this
     */
    public boolean reviewableBy(final String username) {
        final Questionnaire q = getTarget().getQuestionnaire();
        if (q == null) {
            return true;
        }

        // if (authorName.equals(username)) { return false; }
        for (Participant participant : q.getParticipants()) {
            if (participant.getUsername().equals(username)
                    && List.of(participant.getRoles()).contains(Participant.Role.FEEDBACK)) {
                return true;
            }
        }
        return false;
    }

}
