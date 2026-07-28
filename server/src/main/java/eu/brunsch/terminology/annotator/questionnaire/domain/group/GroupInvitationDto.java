package eu.brunsch.terminology.annotator.questionnaire.domain.group;

import eu.brunsch.terminology.annotator.questionnaire.domain.group.Participant.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupInvitationDto {
    /** Identifier value. */
    private Long id;

    /**
     * Display name of the ancestors of this group and the group itself. From most
     * general to most specific, left to right.
     */
    @NonNull
    private String[] names;

    /** Description of the group the user was invited to. */
    @NonNull
    private String description;

    /** Project website. May be null, else it's a valid URL. */
    private String website;

    /** Roles that 8ser was invited as. */
    private Role[] roles;

}
