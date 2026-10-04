package com.qlda.manage_project.modules.timeline.converter;

import com.qlda.manage_project.modules.issue.entity.Issue;
import com.qlda.manage_project.modules.issue.entity.IssueLink;
import com.qlda.manage_project.modules.sprint.entity.Sprint;
import com.qlda.manage_project.modules.timeline.dto.response.TimelineDependencyResponse;
import com.qlda.manage_project.modules.timeline.dto.response.TimelineIssueResponse;
import com.qlda.manage_project.modules.timeline.dto.response.TimelineSprintResponse;
import com.qlda.manage_project.modules.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class TimelineConverter {

    public TimelineIssueResponse toIssueResponse(Issue issue) {
        UserEntity assignee = issue.getAssignee();

        return TimelineIssueResponse.builder()
                .id(issue.getId())
                .parentId(issue.getParent() != null ? issue.getParent().getId() : null)
                .sprintId(issue.getSprint() != null ? issue.getSprint().getId() : null)
                .issueKey(issue.getIssueKey())
                .issueType(issue.getIssueType())
                .title(issue.getTitle())
                .status(issue.getStatus())
                .priority(issue.getPriority())
                .assigneeId(issue.getAssigneeId())
                .assigneeName(assignee != null ? assignee.getFullName() : null)
                .assigneeAvatar(assignee != null ? assignee.getAvatar() : null)
                .startDate(issue.getStartDate())
                .dueDate(issue.getDueDate())
                .version(issue.getVersion())
                .build();
    }

    public TimelineDependencyResponse toDependencyResponse(IssueLink link) {
        return TimelineDependencyResponse.builder()
                .linkId(link.getId())
                .sourceIssueId(link.getSourceIssue().getId())
                .targetIssueId(link.getTargetIssue().getId())
                .build();
    }

    public TimelineSprintResponse toSprintResponse(Sprint sprint) {
        return TimelineSprintResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .status(sprint.getStatus())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .build();
    }
}
