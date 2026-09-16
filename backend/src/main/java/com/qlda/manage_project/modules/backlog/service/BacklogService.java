package com.qlda.manage_project.modules.backlog.service;

import com.qlda.manage_project.modules.backlog.dto.response.IssueBacklogResponse;
import com.qlda.manage_project.modules.issue.enums.IssueStatus;
import com.qlda.manage_project.modules.issue.enums.IssueType;

import java.util.List;

public interface BacklogService {
    List<IssueBacklogResponse> getBacklogIssues(
            Long projectId,
            IssueType issueType,
            Long assigneeId,
            IssueStatus issueStatus,
            String searchKeyword
    );

    default List<IssueBacklogResponse> getBacklogIssues(Long projectId) {
        return getBacklogIssues(projectId, null, null, null, null);
    }
}
