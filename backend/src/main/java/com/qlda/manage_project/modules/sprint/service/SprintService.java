package com.qlda.manage_project.modules.sprint.service;

import com.qlda.manage_project.modules.issue.enums.IssueStatus;
import com.qlda.manage_project.modules.issue.enums.IssueType;
import com.qlda.manage_project.modules.sprint.dto.request.SprintCreateRequest;
import com.qlda.manage_project.modules.sprint.dto.request.SprintUpdateRequest;
import com.qlda.manage_project.modules.sprint.dto.response.SprintResponse;

import java.util.List;

public interface SprintService {
    SprintResponse createSprint(Long projectId, SprintCreateRequest request);

    List<SprintResponse> getSprintsWithIssues(
            Long projectId,
            IssueType issueType,
            Long assigneeId,
            IssueStatus issueStatus,
            String searchKeyword
    );

    default List<SprintResponse> getSprintsWithIssues(Long projectId) {
        return getSprintsWithIssues(projectId, null, null, null, null);
    }

    SprintResponse updateSprint(Long projectId, Long sprintId, Long userId, SprintUpdateRequest request);

    void deleteSprint(Long projectId, Long sprintId, Long userId);

    SprintResponse startSprint(Long projectId, Long sprintId, Long userId);

    SprintResponse completeSprint(Long projectId, Long sprintId, Long userId);
}
