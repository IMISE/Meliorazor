package eu.brunsch.terminology.annotator.questionnaire.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.brunsch.terminology.annotator.questionnaire.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> { }
