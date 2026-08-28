package com.qlda.manage_project.modules.issue.service;

import com.qlda.manage_project.modules.issue.dto.request.IssueCreateRequest;
import com.qlda.manage_project.modules.issue.dto.request.IssueUpdateRequest;
import com.qlda.manage_project.modules.issue.dto.response.IssueResponse;

public interface IssueService {
    IssueResponse createIssue(IssueCreateRequest request, Long reporterId, Long projectId, String projectKey);

    IssueResponse updateIssue(Long issueId, IssueUpdateRequest request, Long actorId);

    void deleteIssue(Long issueId);
}
