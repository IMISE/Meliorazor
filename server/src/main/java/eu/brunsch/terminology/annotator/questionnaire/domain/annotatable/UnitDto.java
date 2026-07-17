package eu.brunsch.terminology.annotator.questionnaire.domain.annotatable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Unit (with a numerical value) of a Data Element. */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UnitDto extends AnnotatableDto {
    /** Minimal value of the numeric unit. */
    private Integer min;

    /** Maximal value of the numeric unit. */
    private Integer max;

    /**
     * Visitor pattern to map DTOs using MapStruct.
     * Stolen from https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
     */
    @Override
    public Annotatable accept(AnnotatableDto.Visitor visitor) {
        return visitor.visit(this);
    }

}
