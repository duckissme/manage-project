package com.qlda.manage_project.modules.backlog.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class IssueBacklogResponse {
    private Long id;
    private String issueKey;
    private String issueType;
    private String status;
    private String priority;
    private LocalDateTime createdAt;
}
