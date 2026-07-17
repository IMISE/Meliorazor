package eu.brunsch.terminology.annotator.questionnaire.domain.annotation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CommentDto extends ActionDto {
    /**
     * Determines how the comment is displayed and handled.
     * May change parent annotation's acceptance status.
     */
    private Comment.Type commentType;

    /**
     * Visitor pattern to map DTOs using MapStruct.
     * Stolen from https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
     */
    @Override
    public Action accept(ActionDto.Visitor visitor) {
        return visitor.visit(this);
    }
}
