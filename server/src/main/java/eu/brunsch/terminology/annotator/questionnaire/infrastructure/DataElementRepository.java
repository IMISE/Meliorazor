package eu.brunsch.terminology.annotator.questionnaire.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.brunsch.terminology.annotator.questionnaire.domain.Questionnaire;
import eu.brunsch.terminology.annotator.questionnaire.domain.annotatable.DataElement;

public interface DataElementRepository
    extends JpaRepository<DataElement, Long> {
    /**
     * Find all data elements grouped in a questionnaire.
     * Only manually for performance reasons:
     * We want to show an overview page without loading the data elements.
     * @param questionnaire the questionnaire whose variables we are fetching
     * @return data elements/variables which are part of the questionnaire
     */
    List<DataElement> findByQuestionnaire(Questionnaire questionnaire);
}
