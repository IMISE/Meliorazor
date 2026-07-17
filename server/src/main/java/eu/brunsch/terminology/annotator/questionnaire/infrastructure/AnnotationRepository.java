package eu.brunsch.terminology.annotator.questionnaire.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.Annotatable;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.Annotation;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.Code;

public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    /**
     * Find all annotations on a given variable.
     * @param variable data element to find annotations of
     * @return all annotations of any status referring to the data element
     */
    List<Annotation> findByTarget(Annotatable target);

    Optional<Annotation> findByTargetAndPredicateAndCode(Annotatable target, String predicate, Code code);
}
