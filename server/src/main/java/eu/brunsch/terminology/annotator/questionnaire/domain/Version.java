package eu.brunsch.terminology.annotator.questionnaire.domain;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Groups annotations of one project into a version.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Version {
    /** Identifier value. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name/title/number/... of the version.
     * MUST be unique for this project.
     */
    private String key;

    /** Additional information. */
    @Lob
    private String description;

    /** Annotations which are part of this version. */
    @ManyToMany
    private Annotation[] annotations;

    /** Questionnaire that is versioned. */
    @ManyToOne
    @JoinColumn(name = "questionnaire_id")
    private Questionnaire questionnaire;

    /** Date and time this comment was submitted at. */
    @CreatedDate
    private Instant submittedAt;
}
