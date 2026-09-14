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
public class IssueResponse {
    private Long id;
    private Long projectId;
    private Long sprintId;
    private String issueKey;
    private IssueType issueType;
    private String title;
    private String description;
    private IssueStatus status;
    private IssuePriority priority;
    private Integer storyPoint;
    private Long reporterId;
    private Long assigneeId;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
