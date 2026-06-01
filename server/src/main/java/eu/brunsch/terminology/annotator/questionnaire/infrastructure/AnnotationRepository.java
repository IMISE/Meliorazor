package eu.brunsch.terminology.annotator.questionnaire.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.brunsch.terminology.annotator.questionnaire.domain.Annotation;
import eu.brunsch.terminology.annotator.questionnaire.domain.DataElement;

public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    // /**
    //  * Finds all annotations with a certain status
    //  * visible to an organsiation.
    //  * @param status current annotation status
    //  * @param organisationId organisation this is visible by
    //  * @return annotations with status and organisation ID
    //  */
    // List<Annotation> findByStatusAndOrganisationId(
    //     Annotation.Status status,
    //     Integer organisationId
    // );

    /**
     * Find all annotations on a given variable.
     * @param variable data element to find annotations of
     * @return all annotations of any status referring to the data element
     */
    List<Annotation> findByVariable(DataElement variable);
}
