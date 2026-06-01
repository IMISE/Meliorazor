package eu.brunsch.terminology.annotator.questionnaire.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
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
public class Code {
    /** URL of CodeSystem. */
    private String system;

    /** Identifier of code. */
    private String code;

    /** Default displayed label - may be in wrong language. */
    @Lob
    private String display;
}
