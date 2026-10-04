package com.qlda.manage_project.modules.timeline.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimelineResponse {
    private List<TimelineIssueResponse> issues;
    private List<TimelineDependencyResponse> dependencies;
    private List<TimelineSprintResponse> sprints;
}
