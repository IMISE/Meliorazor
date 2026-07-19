package eu.brunsch.terminology.annotator.questionnaire.infrastructure;

import java.time.Instant;
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

    /**
     * Finds all annotations with an action of a specific type done by a specific user.
     * @param actionAuthorName author who has action of type
     * @param actionActionType one of "comment", "submission", "version"
     * @param actionSubmittedAt only find results submitted after this date
     * @return all annotations with an action by a given author with a specific type submitted after some date
     */
    List<Annotation> findAllByActionsAuthorNameAndActionsActionTypeAndActionsSubmittedAtAfter(String actionsAuthorName, String actionsActionType, Instant actionsSubmittedAt);


    /**
     * Finds all annotations with an action of a specific type done by a specific user and the annotation has a specific status.
     * @param status desired current status of the annotation
     * @param actionAuthorName author who has action of type
     * @param actionActionType one of "comment", "submission", "version"
     * @return all annotations with a certain status an action by a given author with a specific type.
     */
    List<Annotation> findAllByStatusAndActionsAuthorNameAndActionsActionType(Annotation.Status status, String actionsAuthorName, String actionsActionType);

    /**
     * Finds all annotations with an action of a specific type done by a specific user and the annotation DOES NOT have a given status,
     * meaning it returns every status except the provided one.
     * @param status status the annotation should not have
     * @param actionAuthorName author who has action of type
     * @param actionActionType one of "comment", "submission", "version"
     * @return all annotations missing a certain status with an action by a given author with a specific type.
     */
    List<Annotation> findAllByStatusNotAndActionsAuthorNameAndActionsActionType(Annotation.Status status, String actionsAuthorName, String actionsActionType);

}
