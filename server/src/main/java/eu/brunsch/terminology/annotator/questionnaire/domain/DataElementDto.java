package eu.brunsch.terminology.annotator.questionnaire.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataElementDto {
    /**
     * Unique identifier of this comment.
     * Ignored on creation.
     */
    private Long id;

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
    private ChoiceDto[] choices;

    /** Imported, variable-level metadata. */
    private DataElement.AdditionalInformation[] metadata;

    /**
     * Optionally, annotations of this data element.
     * Ignored on creation.
     */
    private AnnotationDto[] annotations;
}
