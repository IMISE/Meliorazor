package eu.brunsch.terminology.annotator.questionnaire.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChoiceDto {
    /**
     * Unique identifier of this choice.
     * Ignored on creation.
     */
    private Long id;

    /**
     * Used to identify this option on evaluation.
     * Unique in the scope of the referenced data element.
     */
    private String code;

    /**
     * Human-readable label.
    */
    private String name;

    /**
     * Optionally, annotations of this data element.
     * Ignored on creation.
     */
    private AnnotationDto[] annotations;
}
