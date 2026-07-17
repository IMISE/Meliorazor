package eu.brunsch.terminology.annotator.questionnaire.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.Unit;

public interface UnitRepository extends JpaRepository<Unit, Long> {}
