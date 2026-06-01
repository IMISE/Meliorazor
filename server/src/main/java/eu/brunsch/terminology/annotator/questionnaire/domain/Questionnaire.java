package eu.brunsch.terminology.annotator.questionnaire.domain;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single project/study/CRF/...
 * Questionnaires group data elements and govern their annotation.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Questionnaire {
    /** Identifier value. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Name or title of the questionnaire. */
    private String title;

    /** Additional information. */
    @Lob
    private String description;

    /** Configures the annotation process. */
    private QuestionnaireOptions options;

    /** Assigns usernames permissions. */
    @ElementCollection
    private Participant[] participants;

    /** URLs of whitelisted value sets. */
    @ElementCollection
    private FhirValueSetDefinition[] valueSets;

    /**
     * Whether a user with the given username is able to see this questionnaire.
     * This questionnaire should not be displayed to this user if it answers `false`.
     * @param username set on questionnaire and taken from claims 
     * @return whether user is allowed to see this
     */
    public boolean seenBy(final String username) {
        for (Participant participant : participants) {
            if (participant.getUsername().equals(username) && participant.getRoles().length > 0) {
                System.err.println("Worked, innit");
                return true;
            }
        }
        return false;
    }

    /**
     * Whether a user with the given username is able to administer this questionnaire.
     * Editing or deleting (parts of) this project should be rejected if it answers `false`.
     * @param username set on questionnaire and taken from claims 
     * @return whether user is allowed to administer this
     */
    public boolean administeredBy(final String username) {
        for (Participant participant : participants) {
            if (participant.getUsername().equals(username) && List.of(participant.getRoles()).contains(Participant.Role.ADMIN)) {
                return true;
            }
        }
        return false;
    }
}
