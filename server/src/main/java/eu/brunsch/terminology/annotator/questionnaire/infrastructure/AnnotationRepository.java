package eu.brunsch.terminology.annotator.questionnaire.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.Annotatable;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.Annotation;

public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    /**
     * Find all annotations on a given variable.
     * 
     * @param variable data element to find annotations of
     * @return all annotations of any status referring to the data element
     */
    List<Annotation> findByTarget(Annotatable target);

    @Query("select a from Annotation a where a.target.id = ?1 and a.predicate = ?2 and a.code.system = ?3 and a.code.systemVersion = ?4 and a.code.code = ?5")
    Optional<Annotation> findByTargetIdAndPredicateAndCode(Long targetId, String predicate, String codeSystem, String codeSystemVersion, String codeValue);
}
