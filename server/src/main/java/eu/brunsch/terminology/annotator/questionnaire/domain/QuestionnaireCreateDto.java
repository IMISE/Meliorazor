package eu.brunsch.terminology.annotator.questionnaire.domain;

import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.DataElementDto;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionnaireCreateDto {
    /** Name or title of the questionnaire. */
    @NotEmpty
    private String title;

    /** Additional information. */
    @NonNull
    private String description;

    /** ID of the group this belongs to. */
    @NonNull
    private Long groupId;

    /** Configures the annotation process. */
    @NonNull
    private QuestionnaireOptions options;

    /**
     * Data Elements / variables grouped by this questionnaire.
     * May be empty on return, if requested.
     */
    @NotEmpty
    private DataElementDto[] dataElements;

    /** URLs of whitelisted value sets. */
    @NotEmpty
    private FhirValueSetDefinition[] valueSets;

}
