package eu.brunsch.terminology.annotator.questionnaire.domain.group;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Configure a username to have specific permissions in this questionnaire.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Embeddable
@Builder
public class Participant {
    /**
     * Username that gets assigned permissions.
     * Does not have to exist.
     */
    @NotBlank
    private String username;

    /**
     * Roles that get assigned.
     */
    @NotEmpty
    private Role[] roles;

    /** User privileges. */
    public enum Role {
        /** Able to vote on annotations and create questionnaires. */
        FEEDBACK,
        /** Able to annotate resources. */
        ANNOTATE,
        /** Administrative stuff for the project. */
        ADMIN,
        /** Read-only access, but sees *everything*. */
        VIEW,
        /**
         * Dummy role that only allows user to see the project and data elements, but
         * nothing else (annotations etc.).
         */
        INFO
    }

    public boolean hasAdmin() {
        return hasRole(Role.ADMIN);
    }

    public boolean hasAnnotate() {
        return hasRole(Role.ANNOTATE);
    }

    public boolean hasEdit() {
        return hasRole(Role.FEEDBACK);
    }

    public boolean hasView() {
        return hasRole(Role.VIEW);
    }

    public boolean hasAny() {
        return roles != null && roles.length != 0;
    }

    public boolean hasRole(Role role) {
        return Stream.of(getRoles()).anyMatch(r -> role == r || role.equals(r));
    }

    /**
     * Check that the values of the array are valid and can potentially be saved.
     * 
     * @param array (possibly incoming) array of roles or null
     * @return whether the values here could be saved
     */
    public static boolean checkValid(Participant[] array) {
        // no null or empty array
        return array != null && array.length > 0
        // no null or empty values
                && Stream.of(array)
                        .allMatch(p -> p != null && p.username != null && !p.username.isBlank() && p.hasAny()
                                && Stream.of(p.roles).allMatch(r -> r != null))
                // no username doubled
                && Stream.of(array).map(p -> p.username).collect(Collectors.toSet()).size() == array.length;
    }

    public static boolean checkConsistent(Participant[] input, String creatorUsername,
            Collection<String> requiredAdmins) {
        // no invalid properties
        return checkValid(input)
                // we're admins
                && input[0].getUsername().equals(creatorUsername)
                && input[0].hasAdmin()
                // every required admin exists and is an admin
                && (requiredAdmins == null || requiredAdmins
                        .stream()
                        .allMatch(a -> Stream.of(input).anyMatch(p -> a.equals(p.getUsername()) && p.hasAdmin())));
    }

}
