package eu.brunsch.terminology.annotator.questionnaire.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.brunsch.terminology.annotator.questionnaire.domain.Version;

public interface VersionRepository extends JpaRepository<Version, Long> {
    public Boolean existsByKeyAndQuestionnaireId(String key, Long questionnaireId);

    @Query("select v.key from Version v where v.questionnaire = :questionnaireId")
    public List<String> findKeysByQuestionnaireId(@Param("questionnaireId") Long questionnaireId);

    public Optional<Version> findByKeyAndQuestionnaireId(String key, Long questionnaireId);
}
