package com.qlda.manage_project.modules.issue.service;

import com.qlda.manage_project.modules.issue.dto.request.IssueLinkCreateRequest;
import com.qlda.manage_project.modules.issue.dto.response.IssueLinkResponse;

import java.util.List;

public interface IssueLinkService {
    IssueLinkResponse createLink(Long sourceIssueId, IssueLinkCreateRequest request, Long userId);

    List<IssueLinkResponse> getLinksByIssue(Long issueId);

    void deleteLink(Long linkId, Long userId);
}
