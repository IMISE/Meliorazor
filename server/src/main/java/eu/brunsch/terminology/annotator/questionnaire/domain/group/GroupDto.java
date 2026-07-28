package eu.brunsch.terminology.annotator.questionnaire.domain.group;

import eu.brunsch.terminology.annotator.questionnaire.domain.QuestionnaireDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDto {
    /** Identifier value. */
    private Long id;

    /** Display name of the group. */
    @NonNull
    private String name;

    @NonNull
    private String description;

    private String website;

    /** Assigns usernames permissions. */
    private Participant[] participants;

    /** Invited participants need to accept or reject their invitation. */
    private Participant[] invitees;

    private GroupDto[] subGroups;

    private String[] allowedServers;

    private QuestionnaireDto[] projects;

}
