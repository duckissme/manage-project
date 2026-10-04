package com.qlda.manage_project.modules.timeline.service;

import com.qlda.manage_project.modules.timeline.dto.request.IssueScheduleRequest;
import com.qlda.manage_project.modules.timeline.dto.response.TimelineIssueResponse;
import com.qlda.manage_project.modules.timeline.dto.response.TimelineResponse;

public interface TimelineService {

    TimelineResponse getTimeline(Long projectId, Long userId);

    TimelineIssueResponse scheduleIssue(Long projectId, Long issueId, IssueScheduleRequest request, Long userId);
}
