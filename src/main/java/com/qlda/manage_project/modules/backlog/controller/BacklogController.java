package com.qlda.manage_project.modules.backlog.controller;

import com.qlda.manage_project.modules.backlog.dto.response.IssueBacklogResponse;
import com.qlda.manage_project.modules.backlog.service.BacklogService;
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
            @PathVariable Long projectId) {

        List<IssueBacklogResponse> response = backlogService.getBacklogIssues(projectId);

        return ResponseEntity.ok(response);
    }
}
