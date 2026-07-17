package eu.brunsch.terminology.annotator.questionnaire.domain.annotatable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A single data element, questionnaire entry, question, variable.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DataElementDto extends AnnotatableDto {

    /**
     * Human-readable identifier, probably used in evaluation scripts.
     * Will be displayed in squared braces.
     */
    private String variableName;

    /** Data/field type of this variable. */
    private String type;

    /**
     * Extra annotatable with special semantic meaning:
     * "Unit" of the variable, e.g. mg/ml.
     */
    private UnitDto unit;

    /** Distinct options which may be annotated. */
    private ChoiceDto[] choices;

    /** Imported, variable-level metadata. */
    private DataElement.AdditionalInformation[] metadata;

    /**
     * Visitor pattern to map DTOs using MapStruct.
     * Stolen from https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
     */
    @Override
    public Annotatable accept(AnnotatableDto.Visitor visitor) {
        return visitor.visit(this);
    }

}
