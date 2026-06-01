package eu.brunsch.terminology.annotator.questionnaire.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.brunsch.terminology.annotator.questionnaire.domain.Questionnaire;

public interface QuestionnaireRepository
    extends JpaRepository<Questionnaire, Long> { }
