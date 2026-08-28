package com.qlda.manage_project.modules.issue.listener;

import com.qlda.manage_project.modules.issue.entity.IssueHistory;
import com.qlda.manage_project.modules.issue.event.IssueUpdatedEvent;
import com.qlda.manage_project.modules.issue.repository.IssueHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IssueHistoryListener {

    private final IssueHistoryRepository issueHistoryRepository;

    @Async
    @EventListener
    public void handleIssueUpdatedEvent(IssueUpdatedEvent event) {
        for (IssueUpdatedEvent.Change change : event.getChanges()) {
            IssueHistory history = new IssueHistory();
            history.setIssueId(event.getIssueId());
            history.setActorId(event.getActorId());
            history.setFieldName(change.getFieldName());
            history.setOldValue(change.getOldValue());
            history.setNewValue(change.getNewValue());

            issueHistoryRepository.save(history);
        }
    }
}
