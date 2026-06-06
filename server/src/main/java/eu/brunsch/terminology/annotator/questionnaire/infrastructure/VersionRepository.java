package eu.brunsch.terminology.annotator.questionnaire.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.brunsch.terminology.annotator.questionnaire.domain.Version;

public interface VersionRepository
    extends JpaRepository<Version, Long> {
    public Boolean existsByKeyAndQuestionnaireId(String key, Long questionnaireId);
}
