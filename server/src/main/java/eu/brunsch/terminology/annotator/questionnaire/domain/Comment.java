package eu.brunsch.terminology.annotator.questionnaire.domain;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * A reply to an annotation.
 * Could either be a vote to accept/reject it,
 * or a purely informational comment.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Comment {

    /** Identifier value. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Parent annotation this comment is replying to. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Annotation annotation;

    /** Username of the author of this commnent. */
    private String authorName;

    /** Description/"comment" part of the comment. */
    private String contents;

    /** Determines how the comment is displayed and handled. */
    @Enumerated(EnumType.STRING)
    private Type type;

    /** Date and time this comment was submitted at. */
    @CreatedDate
    private Instant submittedAt;

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
