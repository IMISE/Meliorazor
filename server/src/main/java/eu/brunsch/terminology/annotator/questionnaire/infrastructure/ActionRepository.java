package eu.brunsch.terminology.annotator.questionnaire.infrastructure;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.Action;

public interface ActionRepository extends JpaRepository<Action, Long> {
    List<Action> findByAuthorNameAndSubmittedAtAfter(String authorName, Instant submittedAfter);
}
