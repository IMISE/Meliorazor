package eu.brunsch.terminology.annotator.questionnaire.domain;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Information about comments the frontend needs. */
@Data
@NoArgsConstructor
public class CommentDto {

    /**
     * Unique identifier of this comment.
     * Ignored on creation.
     */
    private Long id;

    /** Username of the account which submitted this comment. */
    private String authorName;

    /**
     * Description of the comment.
     * May be a justification for the verdict, a question etc.
     */
    private String contents;

    /**
     * Determines how the comment is displayed and handled.
     * May change parent annotation's acceptance status.
     */
    private Comment.Type type;

    /**
     * Date and time this comment was submitted.
     * Ignored on creation.
     */
    private Instant submittedAt;
}
