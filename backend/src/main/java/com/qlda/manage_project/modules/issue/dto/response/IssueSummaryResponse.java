package com.qlda.manage_project.modules.issue.dto.response;

import com.qlda.manage_project.modules.issue.enums.IssuePriority;
import com.qlda.manage_project.modules.issue.enums.IssueStatus;
import com.qlda.manage_project.modules.issue.enums.IssueType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IssueSummaryResponse {
    private Long id;
    private Long projectId;
    private Long sprintId;
    private Long parentId;
    private String issueKey;
    private IssueType issueType;
    private String title;
    private IssueStatus status;
    private IssuePriority priority;
    private Integer storyPoint;
    private Long assigneeId;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
}
