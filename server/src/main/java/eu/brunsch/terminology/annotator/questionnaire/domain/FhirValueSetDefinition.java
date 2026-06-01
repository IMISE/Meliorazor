package eu.brunsch.terminology.annotator.questionnaire.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Groups options for configuring the annotation process for a single project.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Embeddable
public class FhirValueSetDefinition {

    /**
     * Displayed name of the FHIR server.
     * May be empty when only one is defined.
     * Always non-empty and unique when multiple are defined.
     */
    private String display;

    /** Base URL of the FHIR server. */
    private String server;

    /** URL of the value set. */
    private String valueSet;
}
