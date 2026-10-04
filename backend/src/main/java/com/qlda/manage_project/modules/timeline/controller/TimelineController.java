package com.qlda.manage_project.modules.timeline.controller;

import com.qlda.manage_project.infrastructure.security.CustomUserDetails;
import com.qlda.manage_project.modules.timeline.dto.request.IssueScheduleRequest;
import com.qlda.manage_project.modules.timeline.dto.response.TimelineIssueResponse;
import com.qlda.manage_project.modules.timeline.dto.response.TimelineResponse;
import com.qlda.manage_project.modules.timeline.service.TimelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}")
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping("/timeline")
    public ResponseEntity<TimelineResponse> getTimeline(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getId();
        return ResponseEntity.ok(timelineService.getTimeline(projectId, userId));
    }

    @PatchMapping("/issues/{issueId}/schedule")
    public ResponseEntity<TimelineIssueResponse> scheduleIssue(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @Valid @RequestBody IssueScheduleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getId();
        return ResponseEntity.ok(timelineService.scheduleIssue(projectId, issueId, request, userId));
    }
}
