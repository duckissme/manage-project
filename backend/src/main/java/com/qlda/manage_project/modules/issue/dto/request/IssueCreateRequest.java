package com.qlda.manage_project.modules.issue.dto.request;

import com.qlda.manage_project.modules.issue.enums.IssuePriority;
import com.qlda.manage_project.modules.issue.enums.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IssueCreateRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private IssueType issueType;

    private IssuePriority priority;

    private String description;
    private Long assigneeId;
    private LocalDateTime dueDate;
    private Long parentId;
}
