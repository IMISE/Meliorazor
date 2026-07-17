package eu.brunsch.terminology.annotator.questionnaire.domain.annotation;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("submission")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Submission extends Action {

    /** Self-reported confidence on the correctness of this annotation. */
    private Integer confidence;

    /**
     * Visitor pattern to map DTOs using MapStruct.
     * Stolen from https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
     */
    @Override
    public ActionDto accept(Action.Visitor visitor) {
        return visitor.visit(this);
    }
}
