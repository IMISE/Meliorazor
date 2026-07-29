package eu.brunsch.terminology.annotator.questionnaire.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.brunsch.terminology.annotator.questionnaire.domain.group.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findAllByParticipantsUsername(String username);

    List<Group> findAllByParticipantsUsernameAndParentGroupIsNull(String username);

    List<Group> findAllByInviteesUsername(String username);
}
