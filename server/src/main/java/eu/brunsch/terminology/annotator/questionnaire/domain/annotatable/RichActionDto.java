package eu.brunsch.terminology.annotator.questionnaire.domain.annotatable;

import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.ActionDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RichActionDto {
    private ActionDto action;
    private Long questionnaireId;
    private String questionnaireTitle;
    private Long dataElementId;
    private String dataElementLabel;
    private Long annotationId;
    private String annotationLabel;
    private Long annotatableId;
    private String annotatableLabel;
}
