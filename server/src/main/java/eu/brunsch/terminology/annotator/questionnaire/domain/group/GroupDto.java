package eu.brunsch.terminology.annotator.questionnaire.domain.group;

import eu.brunsch.terminology.annotator.questionnaire.domain.QuestionnaireDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private String website;

    /** Assigns usernames permissions. */
    @NotEmpty
    private Participant[] participants;

    /** Invited participants need to accept or reject their invitation. */
    private Participant[] invitees;

    @NonNull
    private GroupDto[] subGroups;

    @NonNull
    private String[] allowedServers;

    @NonNull
    private QuestionnaireDto[] projects;

}
