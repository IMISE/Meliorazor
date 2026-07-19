package eu.brunsch.terminology.annotator.questionnaire.domain.annotatable;

import eu.brunsch.terminology.annotator.questionnaire.domain.Questionnaire;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Annotatable {
    /** Identifier value. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Description or human-readable label of this field.
     * May be empty, but never null.
     */
    @Lob
    private String label;

    /**
     * Get the questionnaire this element belongs to.
     * @return questionnaire from which permissions can be gathered.
     */
    public abstract Questionnaire getQuestionnaire();

    /**
     * Get the variable this element is or belongs to.
     * @return either itself or its parent
     */
    public abstract DataElement getVariable();

    /**
     * Is this user allowed to annotate this annotatable element?
     * @param username username of user in question
     * @return true if data element this is or belongs to is annotatable by the given username
     */
    public abstract boolean annotatableBy(final String username);

    /**
     * Visitor pattern to map DTOs using MapStruct.
     * Stolen from https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
     */
    public abstract AnnotatableDto accept(Visitor visitor);

    /**
     * Visitor pattern to map DTOs using MapStruct.
     * Stolen from https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
     */
    public interface Visitor {
        public AnnotatableDto visit(Choice choice);
        public AnnotatableDto visit(DataElement dataElement);
        public AnnotatableDto visit(Unit unit);
    }

}
