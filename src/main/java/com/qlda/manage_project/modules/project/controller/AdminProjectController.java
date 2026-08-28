package com.qlda.manage_project.modules.project.controller;

import com.qlda.manage_project.infrastructure.security.CustomUserDetails;
import com.qlda.manage_project.modules.project.dto.request.ProjectCreateReq;
import com.qlda.manage_project.modules.project.dto.request.ProjectUpdateReq;
import com.qlda.manage_project.modules.project.dto.response.ProjectRes;
import com.qlda.manage_project.modules.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminProjectController {
    private final ProjectService projectService;

    @PostMapping("/create-project")
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
}
