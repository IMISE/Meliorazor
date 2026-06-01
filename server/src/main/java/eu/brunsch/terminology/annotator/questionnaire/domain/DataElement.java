package eu.brunsch.terminology.annotator.questionnaire.domain;

import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
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
 * A single data element, questionnaire entry, question.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DataElement {

    /** Identifier value. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    /**
     * Human-readable identifier, probably used in evaluation scripts.
     * Will be displayed in squared braces.
     */
    private String variableName;

    /**
     * Description of this field.
     * May be empty.
     */
    private String label;

    /**
     * Data/field type of this variable.
    */
    private String type;

    /**
     * Distinct options which may be annotated.
     */
    @OneToMany
    private Choice[] choices;

    /** Variable-level metadata not covered by the above fields. */
    @ElementCollection(targetClass = AdditionalInformation.class)
    private AdditionalInformation[] metadata;

    /**
     * Questionnaire this data element belongs to.
     * Questionnaires group the data elements/variables.
     * Also defines some criteria on how to annotate them.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Questionnaire questionnaire;

    /**
     * Additional key-value-pairs we imported.
     * @param key with no additional context
     * @param value as found in imported data
     */
    @Embeddable
    public record AdditionalInformation(String key, String value) { }

    /**
     * Whether a user with the given username is able to annotate this data element and its choices.
     * Annotations by this user on this data element should be rejected if it answers `false`.
     * @param username set on questionnaire and taken from claims 
     * @return whether user is allowed to annotate this
     */
    public boolean annotatableBy(final String username) {
        if (getQuestionnaire() == null) {
            return true;
        }
        for (Participant participant : getQuestionnaire().getParticipants()) {
            if (participant.getUsername().equals(username) && List.of(participant.getRoles()).contains(Participant.Role.ANNOTATE)) {
                return true;
            }
        }
        return false;
    }

}
