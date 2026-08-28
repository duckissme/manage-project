package com.qlda.manage_project.modules.project.controller;

import com.qlda.manage_project.infrastructure.security.CustomUserDetails;
import com.qlda.manage_project.modules.project.dto.response.ProjectDetailRes;
import com.qlda.manage_project.modules.project.dto.response.ProjectRes;
import com.qlda.manage_project.modules.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/")
    public ResponseEntity<List<ProjectRes>> getMyProjects(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getId();
        List<ProjectRes> responses = projectService.getActiveProjectsByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailRes> getProjectDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long currentUserId = customUserDetails.getId();

        ProjectDetailRes response = projectService.getProjectDetail(currentUserId, id);

        return ResponseEntity.ok(response);
    }
}
