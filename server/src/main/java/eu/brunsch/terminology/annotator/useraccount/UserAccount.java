package eu.brunsch.terminology.annotator.useraccount;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single user.
 * Authentication is handled by keycloak.
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserAccount {
    /** Unique identifier. */
    private String username;

    /** Displayed name of the user. */
    private String displayName;

    /** Contact for other users. */
    private String email;

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
