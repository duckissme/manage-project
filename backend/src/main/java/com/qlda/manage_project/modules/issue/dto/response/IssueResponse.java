package com.qlda.manage_project.modules.issue.dto.response;

import com.qlda.manage_project.modules.issue.enums.IssuePriority;
import com.qlda.manage_project.modules.issue.enums.IssueStatus;
import com.qlda.manage_project.modules.issue.enums.IssueType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
    private Long parentId;
    private Long reporterId;
    private Long assigneeId;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<IssueLinkResponse> links;
}
