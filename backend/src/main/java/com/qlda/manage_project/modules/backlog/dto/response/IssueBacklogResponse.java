package com.qlda.manage_project.modules.backlog.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IssueBacklogResponse {
    private Long id;
    private String issueKey;
    private String title;
    private String issueType;
    private String status;
    private String priority;
    private LocalDateTime createdAt;
}
