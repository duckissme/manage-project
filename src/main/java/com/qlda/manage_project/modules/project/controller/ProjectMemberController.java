package com.qlda.manage_project.modules.project.controller;

import com.qlda.manage_project.infrastructure.security.CustomUserDetails;
import com.qlda.manage_project.modules.project.dto.request.AddMemberReq;
import com.qlda.manage_project.modules.project.dto.response.ProjectMemberRes;
import com.qlda.manage_project.modules.project.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @PostMapping
    public ResponseEntity<ProjectMemberRes> addMember(
            @PathVariable Long projectId,
            @RequestBody AddMemberReq request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long currentUserId = customUserDetails.getId();

        ProjectMemberRes response = projectMemberService.addMember(currentUserId, projectId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable("userId") Long userIdToRemove,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long currentUserId = customUserDetails.getId();

        projectMemberService.removeMember(currentUserId, projectId, userIdToRemove);

        return ResponseEntity.noContent().build();
    }
}
