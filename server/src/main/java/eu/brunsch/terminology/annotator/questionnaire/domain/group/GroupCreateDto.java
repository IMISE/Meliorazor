package eu.brunsch.terminology.annotator.questionnaire.domain.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupCreateDto {
    @NotBlank
    private String name;

    @NotNull
    private String description;

    private String website;

    @NonNull
    @NotEmpty
    private Participant[] participants;

    private Long parentGroupId;
}
