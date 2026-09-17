package com.qlda.manage_project.modules.issue.converter;

import com.qlda.manage_project.modules.issue.dto.response.IssueResponse;
import com.qlda.manage_project.modules.issue.dto.response.IssueSummaryResponse;
import com.qlda.manage_project.modules.issue.entity.Issue;
import org.springframework.stereotype.Component;

@Component
public class IssueConverter {

    public IssueResponse mapToResponse(Issue issue) {
        if (issue == null) {
            return null;
        }

        return IssueResponse.builder()
                .id(issue.getId())
                .projectId(issue.getProject() != null ? issue.getProject().getId() : null)
                .sprintId(issue.getSprint() != null ? issue.getSprint().getId() : null)
                .parentId(issue.getParent() != null ? issue.getParent().getId() : null)
                .issueKey(issue.getIssueKey())
                .issueType(issue.getIssueType())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .status(issue.getStatus())
                .priority(issue.getPriority())
                .storyPoint(issue.getStoryPoint())
                .reporterId(issue.getReporterId())
                .assigneeId(issue.getAssigneeId())
                .dueDate(issue.getDueDate())
                .version(issue.getVersion())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .build();
    }

    public IssueSummaryResponse mapToSummaryResponse(Issue issue) {
        if (issue == null) {
            return null;
        }

        return IssueSummaryResponse.builder()
                .id(issue.getId())
                .projectId(issue.getProject() != null ? issue.getProject().getId() : null)
                .sprintId(issue.getSprint() != null ? issue.getSprint().getId() : null)
                .parentId(issue.getParent() != null ? issue.getParent().getId() : null)
                .issueKey(issue.getIssueKey())
                .issueType(issue.getIssueType())
                .title(issue.getTitle())
                .status(issue.getStatus())
                .priority(issue.getPriority())
                .storyPoint(issue.getStoryPoint())
                .assigneeId(issue.getAssigneeId())
                .dueDate(issue.getDueDate())
                .createdAt(issue.getCreatedAt())
                .build();
    }
}
