package eu.brunsch.terminology.annotator.questionnaire.domain;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VersionDto {
    /** Identifier value. */
    private Long id;

    /**
     * Name/title/number/... of the version.
     * MUST be unique for this project.
     */
    private String key;

    /** Additional information. */
    private String description;

    /** Annotations which are part of this version. */
    private AnnotationDto[] annotations;

    /** Date and time this comment was submitted at. */
    private Instant submittedAt;
}
