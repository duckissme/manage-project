package com.qlda.manage_project.modules.issue.controller;

import com.qlda.manage_project.infrastructure.security.CustomUserDetails;
import com.qlda.manage_project.modules.issue.dto.request.IssueCreateRequest;
import com.qlda.manage_project.modules.issue.dto.response.IssueResponse;
import com.qlda.manage_project.modules.issue.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/issues/{issueId}/children")
@RequiredArgsConstructor
public class ChildIssueController {
    private final IssueService issueService;

    @GetMapping
    public ResponseEntity<List<IssueResponse>> getChildIssues(@PathVariable Long issueId) {
        List<IssueResponse> childIssues = issueService.getChildIssues(issueId);
        return ResponseEntity.ok(childIssues);
    }

    @PostMapping
    public ResponseEntity<List<IssueResponse>> createChildIssues(
            @PathVariable Long issueId,
            @RequestBody @Valid List<IssueCreateRequest> requests,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        requests.forEach(req -> req.setParentId(issueId));

        Long projectId = issueService.getProjectIdByIssueId(issueId);
        Long userId = customUserDetails.getId();
        List<IssueResponse> created = issueService.createBulk(requests, userId, projectId);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
