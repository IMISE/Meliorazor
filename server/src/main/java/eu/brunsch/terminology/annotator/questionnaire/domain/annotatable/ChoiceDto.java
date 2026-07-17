package eu.brunsch.terminology.annotator.questionnaire.domain.annotatable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A single select option/choice.
 * Child of exactly one (1) data element.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ChoiceDto extends AnnotatableDto {

    /**
     * Used to identify this option in the eCRF.
     * Allowed to be used for differing purposes on non-select choices like slider ticks.
     */
    private String code;

    /**
     * Visitor pattern to map DTOs using MapStruct.
     * Stolen from https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
     */
    @Override
    public Annotatable accept(AnnotatableDto.Visitor visitor) {
        return visitor.visit(this);
    }
}
