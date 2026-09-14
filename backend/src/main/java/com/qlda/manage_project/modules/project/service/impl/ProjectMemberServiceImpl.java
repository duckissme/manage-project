package com.qlda.manage_project.modules.project.service.impl;

import com.qlda.manage_project.common.exception.ForbiddenException;
import com.qlda.manage_project.common.exception.NotFoundException;
import com.qlda.manage_project.modules.project.exception.ProjectNotFoundException;
import com.qlda.manage_project.modules.project.exception.UserAlreadyInProjectException;
import com.qlda.manage_project.modules.project.dto.request.AddMemberReq;
import com.qlda.manage_project.modules.project.dto.response.ProjectMemberRes;
import com.qlda.manage_project.modules.project.entity.Project;
import com.qlda.manage_project.modules.project.entity.ProjectMember;
import com.qlda.manage_project.modules.project.enums.ProjectRole;
import com.qlda.manage_project.modules.project.repository.ProjectMemberRepository;
import com.qlda.manage_project.modules.project.repository.ProjectRepository;
import com.qlda.manage_project.modules.project.service.ProjectMemberService;
import com.qlda.manage_project.modules.user.entity.UserEntity;
import com.qlda.manage_project.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ProjectMemberRes addMember(Long currentUserId, Long projectId, AddMemberReq request) {

        Project project = projectRepository.findById(projectId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ProjectNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        ProjectMember currentUserMember = projectMemberRepository.findByProjectIdAndUserIdAndIsDeletedFalse(projectId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("Bạn không phải là thành viên của dự án này"));

        if (ProjectRole.OWNER != currentUserMember.getProjectRole()
                && ProjectRole.MANAGER != currentUserMember.getProjectRole()) {
            throw new ForbiddenException("Chỉ OWNER hoặc MANAGER mới có quyền thêm thành viên");
        }

        UserEntity userToAdd = userRepository.findById(request.getUserIdToAdd())
                .orElseThrow(() -> new NotFoundException("Người dùng không tồn tại trong hệ thống"));

        if (projectMemberRepository.existsByProjectIdAndUserIdAndIsDeletedFalse(projectId, request.getUserIdToAdd())) {
            throw new UserAlreadyInProjectException("Người dùng này đã là thành viên của dự án");
        }

        ProjectMember newMember = ProjectMember.builder()
                .project(project)
                .user(userToAdd)
                .projectRole(request.getRole())
                .build();

        ProjectMember savedMember = projectMemberRepository.save(newMember);

        return ProjectMemberRes.builder()
                .id(savedMember.getId())
                .projectId(project.getId())
                .userId(userToAdd.getId())
                .username(userToAdd.getUsername())
                .email(userToAdd.getEmail())
                .role(savedMember.getProjectRole())
                .joinedAt(savedMember.getJoinedAt())
                .build();
    }

    @Override
    @Transactional
    public void removeMember(Long currentUserId, Long projectId, Long userIdToRemove) {
        ProjectMember currentUserMember = projectMemberRepository.findByProjectIdAndUserIdAndIsDeletedFalse(projectId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy dự án hoặc bạn không có quyền truy cập"));

        if (ProjectRole.OWNER != currentUserMember.getProjectRole()
                && ProjectRole.MANAGER != currentUserMember.getProjectRole()) {
            throw new ForbiddenException("Chỉ OWNER hoặc MANAGER mới có quyền gỡ thành viên");
        }

        ProjectMember memberToRemove = projectMemberRepository.findByProjectIdAndUserIdAndIsDeletedFalse(projectId, userIdToRemove)
                .orElseThrow(() -> new NotFoundException("Thành viên này không tồn tại trong dự án"));

        if (memberToRemove.getProjectRole() == ProjectRole.OWNER) {
            throw new ForbiddenException("Không thể gỡ OWNER ra khỏi dự án. Hãy chuyển quyền OWNER trước.");
        }

        if (currentUserId.equals(userIdToRemove)) {
            throw new IllegalArgumentException("Không thể dùng API này để tự rời dự án.");
        }

        projectMemberRepository.deleteMember(memberToRemove.getId());
    }
}
