package eu.brunsch.terminology.annotator.questionnaire.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionnaireEditDto {
    /** Name or title of the questionnaire. */
    private String title;

    /** Additional information. */
    private String description;

    /** Configures the annotation process. */
    private QuestionnaireOptions options;

    /** URLs of whitelisted value sets. */
    private FhirValueSetDefinition[] valueSets;
}
