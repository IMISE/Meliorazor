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
     * Is this a `CodeSystem` or a `ValueSet`?
     *
     * We use `ValueSet`s by expanding them to enable the search functionality;
     * but frequently, the users don't want to search ValueSets.
     * Instead, they want to annotate using specific `CodeSystem`s.
     * This enables that.
     */
    private Boolean implicit;

    /**
     * Displayed name of the `ValueSet` (if `implicit=true`: of the `CodeSystem`).
     * May be empty when only one `ValueSet` is defined for the questionnaire this
     * belongs to.
     * Always non-empty and unique when multiple are defined.
     */
    private String display;

    /**
     * Base URL of the FHIR server.
     * TODO: auth & stuff (tm)
     */
    private String server;

    /** URL of the `ValueSet` (if `implicit=true`: of the `CodeSystem`).. */
    private String url;

    /** Version of the `ValueSet` (if `implicit=true`: of the `CodeSystem`). */
    private String version;
}
