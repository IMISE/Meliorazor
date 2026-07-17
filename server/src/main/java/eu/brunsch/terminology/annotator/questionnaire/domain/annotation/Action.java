package eu.brunsch.terminology.annotator.questionnaire.domain.annotation;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "action_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
public abstract class Action {

    /** Identifier value. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Type of this action, used to differentiate subclasses. */
    @Column(name = "action_type", insertable = false, updatable = false)
    private String actionType;

    /** Username of account with which the action was executed. */
    private String authorName;

    /** Comment or further information */
    @Lob
    private String description;

    /** Date and time this comment was submitted at. */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant submittedAt;

    /** Annotation this action belongs to. */
    @ManyToOne
    private Annotation annotation;

    /**
     * Visitor pattern to map DTOs using MapStruct.
     * Stolen from
     * https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
     */
    public abstract ActionDto accept(Visitor visitor);

    /**
     * Visitor pattern to map DTOs using MapStruct.
     * Stolen from
     * https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
     */
    public interface Visitor {
        public ActionDto visit(Comment comment);

        public ActionDto visit(Submission submission);

        public ActionDto visit(Versioning versioning);
    }
}
