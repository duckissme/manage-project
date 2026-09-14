package com.qlda.manage_project.modules.project.service;

import com.qlda.manage_project.modules.project.dto.request.AddMemberReq;
import com.qlda.manage_project.modules.project.dto.response.ProjectMemberRes;

public interface ProjectMemberService {
    ProjectMemberRes addMember(Long currentUserId, Long projectId, AddMemberReq request);

    void removeMember(Long currentUserId, Long projectId, Long userIdToRemove);
}
