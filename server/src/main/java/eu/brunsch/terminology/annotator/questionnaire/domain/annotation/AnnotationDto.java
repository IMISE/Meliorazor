package eu.brunsch.terminology.annotator.questionnaire.domain.annotation;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Information about annotations the frontend need.
 * Should be a child of an annotatable.
 */
@Data
@NoArgsConstructor
public class AnnotationDto {

    /**
     * Unique identifier of this triple of code, predicate and annotated element.
     */
    private Long id;

    /**
     * Current status of the annotation.
     * Updated on comment submission.
     */
    private Annotation.Status status;

    /**
     * History of actions on this annotation.
     * Includes things such as
     * (1) a new author submitting this triple,
     * (2) someone commenting this triple and
     * (3) someone including this annotation in a version.
     */
    private ActionDto[] actions;

    /** Terminology code the data element is mapped to. */
    private Code code;

    /** Predicate of mapping triple, e.g. skos:exactMatch */
    private String predicate;
}
