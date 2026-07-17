package eu.brunsch.terminology.annotator.questionnaire.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.Action;

public interface ActionRepository extends JpaRepository<Action, Long> { }
