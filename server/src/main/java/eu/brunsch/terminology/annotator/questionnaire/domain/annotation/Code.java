package eu.brunsch.terminology.annotator.questionnaire.domain.annotation;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Groups options for configuring the annotation process for a single project.
 */
@Embeddable
@Data
@NoArgsConstructor
public class Code {
    /** URL of CodeSystem. */
    private String system;

    /** Version of the CodeSystem. */
    private String systemVersion;

    /** Identifier of code. */
    private String code;

    /** Default displayed label - may be in wrong language. */
    @Lob
    private String display;
}
