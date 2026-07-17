package eu.brunsch.terminology.annotator.questionnaire.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.Annotatable;

public interface AnnotatableRepository extends JpaRepository<Annotatable, Long> {}
