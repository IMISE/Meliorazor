package eu.brunsch.terminology.annotator.questionnaire.domain;

import eu.brunsch.terminology.annotator.questionnaire.domain.group.Group;
import eu.brunsch.terminology.annotator.questionnaire.domain.group.Participant;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
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
    @Column(name = "id")
    private Long id;

    /** Name or title of the questionnaire. */
    private String title;

    /** Additional information. */
    @Lob
    private String description;

    /** Configures the annotation process. */
    private QuestionnaireOptions options;

    /** URLs of whitelisted value sets. */
    @ElementCollection
    private FhirValueSetDefinition[] valueSets;

    /** Group this is placed in. Dictates possible servers and participants. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;

    public Participant[] getParticipants() {
        return getGroup() == null ? new Participant[0]
                : getGroup().getParticipants() == null ? new Participant[0] : getGroup().getParticipants();
    }

    public String[] getAllowedServers() {
        return getGroup() == null ? new String[0] : getGroup().getAllowedServers().toArray(String[]::new);
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
        return getGroup() == null ? true : getGroup().administeredBy(username);
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
        return getGroup() == null ? false : getGroup().administeredBy(username);
    }

    /**
     * Do the value sets on this questionnaire comply with the allowed server values
     * of the parent group?
     *
     * @return Whether the Questionnaire should be saved like this.
     */
    public boolean valueSetsAreValid() {
        if (getGroup() == null)
            return true;

        for (FhirValueSetDefinition valueSet : getValueSets()) {
            if (!getGroup().getAllowedServers().contains(valueSet.getServer()))
                return false;
        }

        return true;
    }
}
