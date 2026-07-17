package eu.brunsch.terminology.annotator.questionnaire.domain.annotatable;

import eu.brunsch.terminology.annotator.questionnaire.domain.Questionnaire;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single select option/choice.
 * Child of exactly one (1) data element.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Choice extends Annotatable {

    /**
     * Used to identify this option in the eCRF.
     * Allowed to be used for differing purposes on non-select choices like slider ticks.
     */
    private String code;

    /** Data element this select option belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
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
