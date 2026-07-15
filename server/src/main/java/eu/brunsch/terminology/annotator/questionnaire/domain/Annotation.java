package eu.brunsch.terminology.annotator.questionnaire.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Suggests mapping a data element to a terminology code.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Annotation {

    /** Identifier value. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Terminology code mapped in this annotation. */
    private Code code;

    /**
     * Reviews and notes on this annotation.
     * These directly imply the status of this annotation.
     */
    @OneToMany(mappedBy = "annotation", cascade = CascadeType.ALL)
    private Set<Comment> comments;

    /** Username of account which created this annotation. */
    private String authorName;

    /** Annotated variable. */
    @ManyToOne(fetch = FetchType.LAZY)
    private DataElement variable;

    /**
     * The annotated option.
     * Only populated if this annotates an option of a select.
     * Must be a child of the {@link #variable}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Choice choice;

    /** Self-reported confidence on the correctness of this annotation. */
    private Integer confidence;

    /** Comment on creation */
    private String comment;

    /** Predicate of mapping triple, e.g. skos:exactMatch */
    private String predicate;

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
     * Add a new comment and update the status of this annotation,
     * if the votes change.
     *
     * @param comment the comment to add
     * @param req conditions for marking the annotation as accepted or rejected
     * @return whether the status of this annotation changed
     */
    public boolean addComment(
        final Comment comment,
        final QuestionnaireOptions req
    ) {
        if (comments == null) {
            comments = new HashSet<Comment>();
        }

        comments.add(comment);
        return this.updateStatus(comment.getType(), req);
    }

    /**
     * Remove a comment and update the status of this annotation (if required).
     *
     * @param comment the comment to remove
     * @param req conditions for marking the annotation as accepted or rejected
     * @return whether the status of this annotation changed
     */
    public boolean removeComment(
        final Comment comment,
        final QuestionnaireOptions req
    ) {
        if (comments == null) {
            comments = new HashSet<Comment>();
        } else {
            comments.remove(comment);
        }

        return this.updateStatus(comment.getType(), req);
    }

    /**
     * Update the status of this annotation by re-reading the comments.
     * Will skipped if type of the changed comment is Comment.Type.NOTE;
     * if no comment changed, just pass Comment.Type.ACCEPT.
     *
     * @param changedType type of the added/changed/removed comment
     * @param req conditions for marking the annotation as accepted or rejected
     * @return whether the status changed
     */
    private boolean updateStatus(
        final Comment.Type changedType,
        final QuestionnaireOptions req
    ) {
        // find out the new status of this annotation
        boolean statusChanged = false;
        if (changedType != Comment.Type.NOTE) {
            final var relevant = this.getRelevantVotes();
            final int total = relevant.size();
            final int accept = (int) relevant
                .stream()
                .filter(c -> c.getType() == Comment.Type.ACCEPT).count();
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
    private Collection<Comment> getRelevantVotes() {
        return comments.stream()
            // notes can never change status
            .filter(c -> c.getType() != Comment.Type.NOTE)
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
     * Whether a user with the given username is able to see this annotation.
     * This annotation should not be displayed to this user if it answers `false`.
     * @param username set on annotation and questionnaire and taken from claims 
     * @return whether user is allowed to see this
     */
    public boolean seenBy(final String username) {
        if (authorName.equals(username) || getVariable().getQuestionnaire() == null) {
            return true;
        }

        for (Participant participant : getVariable().getQuestionnaire().getParticipants()) {
            if (participant.getUsername().equals(username)) {
                final List<Participant.Role> roles = List.of(participant.getRoles());
                if (roles.contains(Participant.Role.FEEDBACK) || roles.contains(Participant.Role.VIEW)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Whether a user with the given username is able to review annotations on data elements of this questionnaire.
     * Feedback by this user on data elements of this project should be rejected if it answers `false`.
     * @param username set on annotation and questionnaire and taken from claims 
     * @return whether user is allowed to give feedback on this
     */
    public boolean reviewableBy(final String username) {
        if (getVariable().getQuestionnaire() == null) {
            return true;
        }
        // if (authorName.equals(username)) { return false; }
        for (Participant participant : getVariable().getQuestionnaire().getParticipants()) {
            if (participant.getUsername().equals(username) && List.of(participant.getRoles()).contains(Participant.Role.FEEDBACK)) {
                return true;
            }
        }
        return false;
    }

}
