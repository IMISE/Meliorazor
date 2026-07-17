package eu.brunsch.terminology.annotator.questionnaire.domain.annotatable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import eu.brunsch.terminology.annotator.questionnaire.domain.annotation.AnnotationDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME, 
  include = JsonTypeInfo.As.PROPERTY, 
  property = "type"
)
@JsonSubTypes({ 
  @Type(value = ChoiceDto.class, name = "choice"), 
  @Type(value = DataElementDto.class, name = "dataElement"), 
  @Type(value = UnitDto.class, name = "unit"), 
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class AnnotatableDto {

    /** Identifier value. */
    private Long id;

    /**
     * Description or human-readable label of this field.
     * May be empty, but never null.
     */
    private String label;

    /** Annotations on this element, including their history. */
    private AnnotationDto[] annotations;

    /**
     * Visitor pattern to map DTOs using MapStruct.
     * Stolen from https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
     */
    public abstract Annotatable accept(Visitor visitor);

    /**
     * Visitor pattern to map DTOs using MapStruct.
     * Stolen from https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
     */
    public interface Visitor {
        public Annotatable visit(ChoiceDto choice);
        public Annotatable visit(DataElementDto dataElement);
        public Annotatable visit(UnitDto unit);
    }

}
