package eu.brunsch.terminology.annotator.questionnaire.domain.annotation;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("comment")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Comment extends Action {

    /** Determines how the comment is displayed and handled. */
    @Enumerated(EnumType.STRING)
    private Type commentType;

    /**
     * Visitor pattern to map DTOs using MapStruct.
     * Stolen from https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
     */
    @Override
    public ActionDto accept(Action.Visitor visitor) {
        return visitor.visit(this);
    }

    /**
     * Types that Comments can be (changes their behaviour).
     */
    public enum Type {
        /** Vote to accept the annotation this comment is replying to. */
        ACCEPT,
        /** Vote to reject the annotation this comment is replying to. */
        REJECT,
        /** Comment is purely informational and not considered in vote. */
        NOTE
    }
}
