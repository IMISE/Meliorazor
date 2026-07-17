package eu.brunsch.terminology.annotator.questionnaire.domain.annotation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** A new author submits an annotation */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SubmissionDto extends ActionDto {
    
    /** Self-reported confidence on the correctness of this annotation. */
    private Integer confidence;

    /**
     * Visitor pattern to map DTOs using MapStruct.
     * Stolen from https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
     */
    @Override
    public Action accept(ActionDto.Visitor visitor) {
        return visitor.visit(this);
    }
}
