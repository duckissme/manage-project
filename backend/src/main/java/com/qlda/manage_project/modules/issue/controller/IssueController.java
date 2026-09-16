package com.qlda.manage_project.modules.issue.controller;

import com.qlda.manage_project.infrastructure.security.CustomUserDetails;
import com.qlda.manage_project.modules.issue.dto.request.IssueCreateRequest;
import com.qlda.manage_project.modules.issue.dto.request.IssueUpdateRequest;
import com.qlda.manage_project.modules.issue.dto.request.MoveIssueRequest;
import com.qlda.manage_project.modules.issue.dto.response.IssueResponse;
import com.qlda.manage_project.modules.issue.enums.IssueType;
import com.qlda.manage_project.modules.issue.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    @PostMapping("/projects/{projectId}/issues")
    public ResponseEntity<IssueResponse> createIssue(
            @PathVariable Long projectId,
            @Valid @RequestBody IssueCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getId();

        IssueResponse response = issueService.createIssue(request, userId, projectId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/projects/{projectId}/issues/bulk")
    public ResponseEntity<List<IssueResponse>> createBulkIssues (
            @PathVariable Long projectId,
            @Valid @RequestBody List<IssueCreateRequest> requests,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getId();

        List<IssueResponse> responses = issueService.createBulk(requests, userId, projectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PutMapping("/issues/{issueId}")
    public ResponseEntity<IssueResponse> updateIssue(
            @PathVariable Long issueId,
            @Valid @RequestBody IssueUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getId();
        IssueResponse response = issueService.updateIssue(issueId, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/issues/{issueId}")
    public ResponseEntity<Void> deleteIssue(@PathVariable Long issueId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getId();

        issueService.deleteIssue(issueId, userId);
        return ResponseEntity.noContent().build(); // HTTP 204
    }

    @GetMapping("/issues/{issueId}")
    public ResponseEntity<IssueResponse> viewDetailIssue(@PathVariable Long issueId) {
        IssueResponse response = issueService.viewDetailIssue(issueId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/projects/{projectId}/issues/{issueId}/move")
    public ResponseEntity<Void> moveIssue(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @Valid @RequestBody MoveIssueRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getId();

        issueService.moveIssue(projectId, issueId, request, userId);

        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

    @GetMapping("/projects/{projectId}/issues")
    public ResponseEntity<List<IssueResponse>> getIssuesByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) IssueType type) {

        List<IssueResponse> responses = issueService.getIssues(projectId, type);
        return ResponseEntity.ok(responses);
    }
}
