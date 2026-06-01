package eu.brunsch.terminology.annotator.questionnaire.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import eu.brunsch.terminology.annotator.useraccount.UserAccount;

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
    private UserAccount.Role[] roles;

}
