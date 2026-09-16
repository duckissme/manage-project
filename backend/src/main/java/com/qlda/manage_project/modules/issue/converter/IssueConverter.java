package com.qlda.manage_project.modules.issue.converter;

import com.qlda.manage_project.modules.issue.dto.response.IssueResponse;
import com.qlda.manage_project.modules.issue.entity.Issue;
import org.springframework.stereotype.Component;

@Component
public class IssueConverter {

    public IssueResponse mapToResponse(Issue issue) {
        if (issue == null) {
            return null;
        }

        IssueResponse response = new IssueResponse();
        response.setId(issue.getId());
        response.setProjectId(issue.getProject().getId());
        response.setSprintId(issue.getSprint().getId());
        response.setIssueKey(issue.getIssueKey());
        response.setIssueType(issue.getIssueType());
        response.setTitle(issue.getTitle());
        response.setDescription(issue.getDescription());
        response.setStatus(issue.getStatus());
        response.setPriority(issue.getPriority());
        response.setStoryPoint(issue.getStoryPoint());
        response.setReporterId(issue.getReporterId());
        response.setAssigneeId(issue.getAssigneeId());
        response.setDueDate(issue.getDueDate());
        response.setCreatedAt(issue.getCreatedAt());
        response.setUpdatedAt(issue.getUpdatedAt());

        return response;
    }
}
