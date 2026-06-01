package eu.brunsch.terminology.annotator.questionnaire.domain;

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

}
