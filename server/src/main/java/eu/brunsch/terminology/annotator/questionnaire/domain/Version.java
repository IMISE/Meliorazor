package eu.brunsch.terminology.annotator.questionnaire.domain;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.Annotation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Groups annotations of one project into a version.
 */
@Entity(name = "Version")
@Table(
    uniqueConstraints = @UniqueConstraint(
        name = "UniqueVersionKeyPerQuestionnaire",
        columnNames = { "key", "questionnaire_id" }
    )
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Version {
    /** Identifier value. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name/title/number/... of the version.
     * MUST be unique for this project.
     */
    @Column(name = "key")
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
