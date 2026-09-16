package com.qlda.manage_project.modules.issue.listener;

import com.qlda.manage_project.modules.issue.entity.Issue;
import com.qlda.manage_project.modules.issue.entity.IssueHistory;
import com.qlda.manage_project.modules.issue.event.IssueUpdatedEvent;
import com.qlda.manage_project.modules.issue.repository.IssueHistoryRepository;
import com.qlda.manage_project.modules.user.entity.UserEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class IssueHistoryListener {

    private final IssueHistoryRepository issueHistoryRepository;
    private final EntityManager entityManager;

    @Async
    @EventListener
    @Transactional
    public void handleIssueUpdatedEvent(IssueUpdatedEvent event) {
        Issue issueProxy = entityManager.getReference(Issue.class, event.getIssueId());
        UserEntity actorProxy = entityManager.getReference(UserEntity.class, event.getActorId());

        for (IssueUpdatedEvent.Change change : event.getChanges()) {
            IssueHistory history = new IssueHistory();
            history.setIssue(issueProxy);
            history.setActor(actorProxy);
            history.setFieldName(change.getFieldName());
            history.setOldValue(change.getOldValue());
            history.setNewValue(change.getNewValue());

            issueHistoryRepository.save(history);
        }
    }
}
