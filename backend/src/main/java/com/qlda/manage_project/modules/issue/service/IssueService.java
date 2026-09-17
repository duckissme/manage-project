package com.qlda.manage_project.modules.issue.service;

import com.qlda.manage_project.modules.issue.dto.request.IssueCreateRequest;
import com.qlda.manage_project.modules.issue.dto.request.IssueUpdateRequest;
import com.qlda.manage_project.modules.issue.dto.request.MoveIssueRequest;
import com.qlda.manage_project.modules.issue.dto.response.IssueResponse;
import com.qlda.manage_project.modules.issue.dto.response.IssueSummaryResponse;
import com.qlda.manage_project.modules.issue.enums.IssueType;

import java.util.List;

public interface IssueService {
    IssueSummaryResponse createIssue(IssueCreateRequest request, Long reporterId, Long projectId);

    IssueResponse updateIssue(Long issueId, IssueUpdateRequest request, Long actorId);

    void deleteIssue(Long issueId, Long userId, Integer version);

    default void deleteIssue(Long issueId, Long userId) {
        deleteIssue(issueId, userId, null);
    }

    IssueResponse viewDetailIssue(Long issueId);

    void moveIssue(Long projectId, Long issueId, MoveIssueRequest request, Long actorId);

    List<IssueSummaryResponse> getChildIssues(Long parentId);

    Long getProjectIdByIssueId(Long issueId);

    List<IssueSummaryResponse> createBulk(List<IssueCreateRequest> requests, Long reporterId, Long projectId);

    List<IssueSummaryResponse> getIssues(Long projectId, IssueType type, String keyword, Long excludeIssueId, Integer limit);

    default List<IssueSummaryResponse> getIssues(Long projectId, IssueType type) {
        return getIssues(projectId, type, null, null, null);
    }
}
