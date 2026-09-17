package com.qlda.manage_project.modules.issue.dto.response;

import com.qlda.manage_project.modules.issue.enums.IssueLinkType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IssueLinkResponse {
    private Long id;
    private IssueLinkType linkType;
    private String relationship;
    private IssueSummaryResponse linkedIssue;
    private LocalDateTime createdAt;
}
