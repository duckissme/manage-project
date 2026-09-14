package com.qlda.manage_project.modules.backlog.service;

import com.qlda.manage_project.modules.backlog.dto.response.IssueBacklogResponse;

import java.util.List;

public interface BacklogService {
    List<IssueBacklogResponse> getBacklogIssues(Long projectId);
}
