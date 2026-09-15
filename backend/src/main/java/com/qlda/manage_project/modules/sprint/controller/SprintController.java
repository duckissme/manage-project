package com.qlda.manage_project.modules.sprint.controller;

import com.qlda.manage_project.infrastructure.security.CustomUserDetails;
import com.qlda.manage_project.modules.sprint.dto.request.SprintCreateRequest;
import com.qlda.manage_project.modules.sprint.dto.request.SprintUpdateRequest;
import com.qlda.manage_project.modules.sprint.dto.response.SprintResponse;
import com.qlda.manage_project.modules.sprint.service.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/sprints")
public class SprintController {
    private final SprintService sprintService;

    @PostMapping
    public ResponseEntity<SprintResponse> createSprint(
            @PathVariable Long projectId,
            @Valid @RequestBody SprintCreateRequest request) {

        SprintResponse response = sprintService.createSprint(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SprintResponse>> getSprints(@PathVariable Long projectId) {
        List<SprintResponse> response = sprintService.getSprintsWithIssues(projectId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{sprintId}")
    public ResponseEntity<SprintResponse> updateSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @RequestBody SprintUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getId();
        SprintResponse response = sprintService.updateSprint(projectId, sprintId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sprintId}")
    public ResponseEntity<Void> deleteSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getId();
        sprintService.deleteSprint(projectId, sprintId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sprintId}/start")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<SprintResponse> startSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getId();
        SprintResponse response = sprintService.startSprint(projectId, sprintId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sprintId}/complete")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<SprintResponse> completeSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getId();
        SprintResponse response = sprintService.completeSprint(projectId, sprintId, userId);
        return ResponseEntity.ok(response);
    }
}
