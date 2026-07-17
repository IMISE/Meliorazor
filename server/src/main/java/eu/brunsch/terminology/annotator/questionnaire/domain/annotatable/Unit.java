package eu.brunsch.terminology.annotator.questionnaire.domain.annotatable;

import eu.brunsch.terminology.annotator.questionnaire.domain.Questionnaire;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Unit (with a numerical value) of a Data Element. */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Unit extends Annotatable {

    /** Minimal value of the numeric unit. */
    private Integer min;

    /** Maximal value of the numeric unit. */
    private Integer max;

    /** Data element described by this unit. */
    @OneToOne(fetch = FetchType.LAZY)
    private DataElement variable;


    @Override
    public Questionnaire getQuestionnaire() {
        return variable == null ? null : variable.getQuestionnaire();
    }


    @Override
    public boolean annotatableBy(final String username) {
        return getVariable().annotatableBy(username);
    }

    /**
     * Visitor pattern to map DTOs using MapStruct.
     * Stolen from https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
     */
    @Override
    public AnnotatableDto accept(Annotatable.Visitor visitor) {
        return visitor.visit(this);
    }

}
