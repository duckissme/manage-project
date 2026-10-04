package com.qlda.manage_project.modules.timeline.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimelineDependencyResponse {
    private Long linkId;
    private Long sourceIssueId;
    private Long targetIssueId;
}
