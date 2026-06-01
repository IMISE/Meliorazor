package eu.brunsch.terminology.annotator.questionnaire.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Configure a username to have specific permissions in this questionnaire.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Embeddable
public class Participant {
    /**
     * Username that gets assigned permissions.
     * Does not have to exist.
     */
    private String username;

    /**
     * Roles that get assigned.
     */
    private Role[] roles;

    /** User privileges. */
    public enum Role {
        /** Able to vote on annotations and create questionnaires. */
        FEEDBACK,
        /** Able to annotate resources. */
        ANNOTATE,
        /** Administrative stuff for the project. */
        ADMIN,
        /** Read-only access. */
        VIEW,
    }

}
