package eu.brunsch.terminology.annotator.questionnaire.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single select option/choice.
 * Child of one (1) data element.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Choice {

    /** Identifier value. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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
     * Data element this select option belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private DataElement variable;
}
