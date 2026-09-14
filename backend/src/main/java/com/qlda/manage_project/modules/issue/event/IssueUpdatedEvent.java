package com.qlda.manage_project.modules.issue.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class IssueUpdatedEvent {
    private Long issueId;
    private Long actorId;
    private List<Change> changes;

    @Getter
    @AllArgsConstructor
    public static class Change {
        private String fieldName;
        private String oldValue;
        private String newValue;
    }
}
