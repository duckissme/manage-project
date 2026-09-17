package com.qlda.manage_project.modules.issue.controller;

import com.qlda.manage_project.infrastructure.security.CustomUserDetails;
import com.qlda.manage_project.modules.issue.dto.request.IssueLinkCreateRequest;
import com.qlda.manage_project.modules.issue.dto.response.IssueLinkResponse;
import com.qlda.manage_project.modules.issue.service.IssueLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class IssueLinkController {

    private final IssueLinkService issueLinkService;

    @PostMapping("/issues/{issueId}/links")
    public ResponseEntity<IssueLinkResponse> createLink(
            @PathVariable Long issueId,
            @Valid @RequestBody IssueLinkCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getId();
        IssueLinkResponse response = issueLinkService.createLink(issueId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/issues/{issueId}/links")
    public ResponseEntity<List<IssueLinkResponse>> getLinks(@PathVariable Long issueId) {
        List<IssueLinkResponse> links = issueLinkService.getLinksByIssue(issueId);
        return ResponseEntity.ok(links);
    }

    @DeleteMapping("/issues/links/{linkId}")
    public ResponseEntity<Void> deleteLink(
            @PathVariable Long linkId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getId();
        issueLinkService.deleteLink(linkId, userId);
        return ResponseEntity.noContent().build();
    }
}
