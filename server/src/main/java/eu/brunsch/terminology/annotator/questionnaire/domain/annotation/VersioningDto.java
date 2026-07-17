package eu.brunsch.terminology.annotator.questionnaire.domain.annotation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Someone adds this annotation to a version. */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class VersioningDto extends ActionDto {
    /** Key of the created version. */
    public String versionKey;

    /**
     * Visitor pattern to map DTOs using MapStruct.
     * Stolen from https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
     */
    @Override
    public Action accept(ActionDto.Visitor visitor) {
        return visitor.visit(this);
    }
}
