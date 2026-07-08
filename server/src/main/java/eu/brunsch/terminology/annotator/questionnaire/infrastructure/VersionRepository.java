package eu.brunsch.terminology.annotator.questionnaire.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import eu.brunsch.terminology.annotator.questionnaire.domain.Version;

public interface VersionRepository extends JpaRepository<Version, Long> {
    public Boolean existsByKeyAndQuestionnaireId(String key, Long questionnaireId);

    @Query("SELECT v.key FROM Version v WHERE v.questionnaire_id = ?1", nativeQuery = true)
    public List<String> findKeysByQuestionnaireId(Long questionnaireId);

    public Optional<Version> findByKeyAndQuestionnaireId(String key, Long questionnaireId);
}
