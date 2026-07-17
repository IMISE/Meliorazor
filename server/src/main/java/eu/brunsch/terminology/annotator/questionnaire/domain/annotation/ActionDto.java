package eu.brunsch.terminology.annotator.questionnaire.domain.annotation;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "actionType")
@JsonSubTypes({
    @Type(value = CommentDto.class, name = "comment"),
    @Type(value = SubmissionDto.class, name = "submission"),
    @Type(value = VersioningDto.class, name = "version"),
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class ActionDto {
  /** Identifier value. Ignored on creation. */
  private Long id;

  /** Type of this action, used to differentiate subclasses. */
  protected String actionType;

  /**
   * Username of account with which the action was executed.
   * Ignored on creation
   */
  private String authorName;

  /** Comment or further information */
  private String description;

  /**
   * Date and time this comment was submitted at.
   * Ignored on creation
   */
  private Instant submittedAt;

  /**
   * Visitor pattern to map DTOs using MapStruct.
   * Stolen from
   * https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
   */
  public abstract Action accept(Visitor visitor);

  /**
   * Visitor pattern to map DTOs using MapStruct.
   * Stolen from
   * https://www.baeldung.com/java-mapstruct-inheritance#1-applying-the-visitor-pattern.
   */
  public interface Visitor {
    public Action visit(CommentDto comment);

    public Action visit(SubmissionDto submission);

    public Action visit(VersioningDto versioning);
  }
}
