package com.qlda.manage_project.modules.project.controller;

import com.qlda.manage_project.infrastructure.security.CustomUserDetails;
import com.qlda.manage_project.modules.project.dto.request.ProjectCreateReq;
import com.qlda.manage_project.modules.project.dto.request.ProjectUpdateReq;
import com.qlda.manage_project.modules.project.dto.response.ProjectDetailRes;
import com.qlda.manage_project.modules.project.dto.response.ProjectRes;
import com.qlda.manage_project.modules.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectRes> createProject(
            @RequestBody ProjectCreateReq request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getId();
        ProjectRes response = projectService.createProject(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getId();
        projectService.deleteProject(userId, projectId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectRes> updateProject(
            @RequestBody ProjectUpdateReq request,
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getId();
        ProjectRes response = projectService.updateProject(userId, projectId, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProjectRes>> getMyProjects(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getId();
        List<ProjectRes> responses = projectService.getActiveProjectsByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailRes> getProjectDetail(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long currentUserId = customUserDetails.getId();

        ProjectDetailRes response = projectService.getProjectDetail(currentUserId, projectId);

        return ResponseEntity.ok(response);
    }
}
