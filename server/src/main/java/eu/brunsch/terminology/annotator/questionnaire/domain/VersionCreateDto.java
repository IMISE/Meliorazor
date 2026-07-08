package eu.brunsch.terminology.annotator.questionnaire.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Groups together annotations in a version.
 */
@Data
@NoArgsConstructor
public class VersionCreateDto {
    /**
     * Name/title/number/... of the version.
     * MUST be unique in the context of this project.
     */
    private String key;

    /** Additional information. */
    private String description;

    /** IDs of the annotations in this version. */
    private Long[] annotationIds;

}
