package com.qlda.manage_project.modules.issue.service;

import com.qlda.manage_project.modules.issue.dto.request.IssueCreateRequest;
import com.qlda.manage_project.modules.issue.dto.request.IssueUpdateRequest;
import com.qlda.manage_project.modules.issue.dto.request.MoveIssueRequest;
import com.qlda.manage_project.modules.issue.dto.response.IssueResponse;
import com.qlda.manage_project.modules.issue.enums.IssueType;

import java.util.List;

public interface IssueService {
    IssueResponse createIssue(IssueCreateRequest request, Long reporterId, Long projectId);

    IssueResponse updateIssue(Long issueId, IssueUpdateRequest request, Long actorId);

    void deleteIssue(Long issueId, Long userId);

    IssueResponse viewDetailIssue(Long issueId);

    void moveIssue(Long projectId, Long issueId, MoveIssueRequest request, Long actorId);

    List<IssueResponse> getChildIssues(Long parentId);

    Long getProjectIdByIssueId(Long issueId);

    List<IssueResponse> createBulk(List<IssueCreateRequest> requests, Long reporterId, Long projectId);

    List<IssueResponse> getIssues(Long projectId, IssueType type);
}
