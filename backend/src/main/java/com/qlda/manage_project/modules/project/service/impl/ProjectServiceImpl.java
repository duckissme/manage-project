package com.qlda.manage_project.modules.project.service.impl;

import com.qlda.manage_project.common.exception.ForbiddenException;
import com.qlda.manage_project.common.exception.NotFoundException;
import com.qlda.manage_project.modules.project.converter.ProjectConverter;
import com.qlda.manage_project.modules.project.dto.request.ProjectCreateReq;
import com.qlda.manage_project.modules.project.dto.request.ProjectUpdateReq;
import com.qlda.manage_project.modules.project.dto.response.ProjectDetailRes;
import com.qlda.manage_project.modules.project.dto.response.ProjectMemberRes;
import com.qlda.manage_project.modules.project.dto.response.ProjectRes;
import com.qlda.manage_project.modules.project.entity.Project;
import com.qlda.manage_project.modules.project.entity.ProjectMember;
import com.qlda.manage_project.modules.project.enums.ProjectRole;
import com.qlda.manage_project.modules.project.enums.ProjectStatus;
import com.qlda.manage_project.modules.project.exception.ProjectNotFoundException;
import com.qlda.manage_project.modules.project.repository.ProjectMemberRepository;
import com.qlda.manage_project.modules.project.repository.ProjectRepository;
import com.qlda.manage_project.modules.project.service.ProjectService;
import com.qlda.manage_project.modules.user.entity.UserEntity;
import com.qlda.manage_project.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectConverter projectConverter;

    @Override
    @Transactional
    public ProjectRes createProject(Long userId, ProjectCreateReq request) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        String formattedKey = request.getProjectKey().trim().toUpperCase().replaceAll("\\s+", "");
        if (projectRepository.existsByProjectKey(formattedKey)) {
            throw new IllegalArgumentException("Project Key '" + formattedKey + "' đã tồn tại trên hệ thống!");
        }

        Project newProject = Project.builder()
                .projectKey(formattedKey)
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .deadline(request.getDeadline())
                .status(ProjectStatus.PLANNING)
                .build();

        Project savedProject = projectRepository.save(newProject);

        ProjectMember owner = ProjectMember.builder()
                .project(savedProject)
                .user(user)
                .projectRole(ProjectRole.OWNER)
                .build();

        projectMemberRepository.save(owner);

        return projectConverter.mapToResponse(savedProject);
    }

    @Override
    public List<ProjectRes> getActiveProjectsByUserId(Long userId) {
        List<Project> projects = projectRepository.findActiveProjectsByUserId(userId);

        return projects.stream()
                .map(projectConverter::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProject(Long currentUserId, Long projectId) {
        projectRepository.findById(projectId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ProjectNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        ProjectMember currentUserMember = projectMemberRepository.findByProjectIdAndUserIdAndIsDeletedFalse(projectId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("Bạn không phải là thành viên của dự án này"));

        if (ProjectRole.OWNER != currentUserMember.getProjectRole()) {
            throw new ForbiddenException("Chỉ OWNER hoặc MANAGER mới có quyền thêm thành viên");
        }

        projectRepository.deleteProject(projectId);
    }

    @Override
    public ProjectDetailRes getProjectDetail(Long currentUserId, Long projectId) {

        Project project = projectRepository.findById(projectId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ProjectNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        boolean isMember = projectMemberRepository.existsByProjectIdAndUserIdAndIsDeletedFalse(projectId, currentUserId);
        if (!isMember) {
            throw new ForbiddenException("Bạn không có quyền xem chi tiết dự án này");
        }

        List<ProjectMember> activeMembers = projectMemberRepository.findByProjectIdAndIsDeletedFalse(projectId);

        List<ProjectMemberRes> memberResponses = activeMembers.stream()
                .map(member -> ProjectMemberRes.builder()
                        .id(member.getId())
                        .projectId(project.getId())
                        .userId(member.getUser().getId())
                        .username(member.getUser().getUsername())
                        .email(member.getUser().getEmail())
                        .role(member.getProjectRole())
                        .joinedAt(member.getJoinedAt())
                        .build())
                .collect(Collectors.toList());

        return ProjectDetailRes.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .deadline(project.getDeadline())
                .members(memberResponses)
                .build();
    }

    @Override
    @Transactional
    public ProjectRes updateProject(Long currentUserId, Long projectId, ProjectUpdateReq request) {

        Project project = projectRepository.findById(projectId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ProjectNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        ProjectMember currentUserMember = projectMemberRepository.findByProjectIdAndUserIdAndIsDeletedFalse(projectId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("Bạn không phải là thành viên của dự án này"));

        if (ProjectRole.OWNER != currentUserMember.getProjectRole()
                && ProjectRole.MANAGER != currentUserMember.getProjectRole()) {
            throw new ForbiddenException("Chỉ OWNER hoặc MANAGER mới có quyền chỉnh sửa dự án");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());
        project.setStartDate(request.getStartDate());
        project.setDeadline(request.getDeadline());

        Project updatedProject = projectRepository.save(project);

        return projectConverter.mapToResponse(updatedProject);
    }
}
