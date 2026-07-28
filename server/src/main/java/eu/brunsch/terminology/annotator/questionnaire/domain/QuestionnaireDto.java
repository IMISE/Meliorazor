package eu.brunsch.terminology.annotator.questionnaire.domain;

import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.DataElementDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionnaireDto {
    /**
     * Identifier value.
     * Ignored on creation.
     */
    private Long id;

    /** Name or title of the questionnaire. */
    private String title;

    /** Additional information. */
    private String description;

    /** Configures the annotation process. */
    private QuestionnaireOptions options;

    /**
     * Data Elements / variables grouped by this questionnaire.
     * May be empty on return, if requested.
     */
    private DataElementDto[] dataElements;

    /** URLs of whitelisted value sets. */
    private FhirValueSetDefinition[] valueSets;
}
