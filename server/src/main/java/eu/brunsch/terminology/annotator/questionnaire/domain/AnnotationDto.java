package eu.brunsch.terminology.annotator.questionnaire.domain;

import lombok.Data;

/** Information about annotations the frontend needs. */
@Data
public class AnnotationDto {

    /** Unique identifier of this annotation. */
    private Long id;

    /** Username of the account which introduced this annotation/mapping request. */
    private String authorName;

    /** Comments in reply to this annotation. */
    private CommentDto[] comments;

    /**
     * Current status of the annotation.
     * Updated on comment submission.
     */
    private Annotation.Status status;

    /** Terminology code the data element is mapped to. */
    private Code code;

    /** Self-reported value on how confident the author is in this mapping. */
    private Integer confidence;

    /** Comment on creation */
    private String comment;

    /** Predicate of mapping triple, e.g. skos:exactMatch */
    private String predicate;

    // TODO SSSOM stuff
}
