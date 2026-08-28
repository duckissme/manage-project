package com.qlda.manage_project.modules.project.service;

import com.qlda.manage_project.modules.project.dto.request.ProjectCreateReq;
import com.qlda.manage_project.modules.project.dto.request.ProjectUpdateReq;
import com.qlda.manage_project.modules.project.dto.response.ProjectDetailRes;
import com.qlda.manage_project.modules.project.dto.response.ProjectRes;

import java.util.List;

public interface ProjectService {
    ProjectRes createProject(Long userId, ProjectCreateReq request);

    List<ProjectRes> getActiveProjectsByUserId(Long userId);

    void deleteProject(Long userId, Long projectId);

    ProjectDetailRes getProjectDetail(Long currentUserId, Long projectId);

    ProjectRes updateProject(Long currentUserId, Long projectId, ProjectUpdateReq request);
}
