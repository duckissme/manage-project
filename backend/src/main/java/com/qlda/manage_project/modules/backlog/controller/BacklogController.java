package com.qlda.manage_project.modules.backlog.controller;

import com.qlda.manage_project.modules.backlog.dto.response.IssueBacklogResponse;
import com.qlda.manage_project.modules.backlog.service.BacklogService;
import com.qlda.manage_project.modules.issue.enums.IssueStatus;
import com.qlda.manage_project.modules.issue.enums.IssueType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/backlog")
@RequiredArgsConstructor
public class BacklogController {

    private final BacklogService backlogService;

    @GetMapping
    public ResponseEntity<List<IssueBacklogResponse>> getBacklog(
            @PathVariable Long projectId,
            @RequestParam(required = false) IssueType issueType,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) IssueStatus issueStatus,
            @RequestParam(required = false) String searchKeyword) {

        List<IssueBacklogResponse> response = backlogService.getBacklogIssues(
                projectId, issueType, assigneeId, issueStatus, searchKeyword
        );

        return ResponseEntity.ok(response);
    }
}
