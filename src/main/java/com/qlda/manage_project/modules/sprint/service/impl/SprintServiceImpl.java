package com.qlda.manage_project.modules.sprint.service.impl;

import com.qlda.manage_project.common.exception.ForbiddenException;
import com.qlda.manage_project.common.exception.NotFoundException;
import com.qlda.manage_project.modules.backlog.dto.response.IssueBacklogResponse;
import com.qlda.manage_project.modules.issue.entity.Issue;
import com.qlda.manage_project.modules.issue.repository.IssueRepository;
import com.qlda.manage_project.modules.project.entity.ProjectMember;
import com.qlda.manage_project.modules.project.enums.ProjectRole;
import com.qlda.manage_project.modules.project.exception.ProjectNotFoundException;
import com.qlda.manage_project.modules.project.repository.ProjectMemberRepository;
import com.qlda.manage_project.modules.project.repository.ProjectRepository;
import com.qlda.manage_project.modules.sprint.dto.request.SprintCreateRequest;
import com.qlda.manage_project.modules.sprint.dto.request.SprintUpdateRequest;
import com.qlda.manage_project.modules.sprint.dto.response.SprintResponse;
import com.qlda.manage_project.modules.sprint.entity.Sprint;
import com.qlda.manage_project.modules.sprint.enums.SprintStatus;
import com.qlda.manage_project.modules.sprint.repository.SprintRepository;
import com.qlda.manage_project.modules.sprint.service.SprintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SprintServiceImpl implements SprintService {
    private final SprintRepository sprintRepository;
    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional
    public SprintResponse createSprint(Long projectId, SprintCreateRequest request) {
        projectRepository.findById(projectId).filter(p -> !p.isDeleted()).orElseThrow(() -> new ProjectNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        String sprintName = request.getName();
        if (sprintName == null || sprintName.trim().isEmpty()) {
            long totalSprints = sprintRepository.countByProjectId(projectId);
            sprintName = "Sprint " + (totalSprints + 1);
            log.info("Auto-generated sprint name: {} for Project ID: {}", sprintName, projectId);
        }

        Sprint newSprint = Sprint.builder()
                .projectId(projectId)
                .name(sprintName.trim())
                .goal(request.getGoal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(SprintStatus.PENDING)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Sprint savedSprint = sprintRepository.save(newSprint);

        return SprintResponse.builder().id(savedSprint.getId()).projectId(savedSprint.getProjectId()).name(savedSprint.getName()).goal(savedSprint.getGoal()).startDate(savedSprint.getStartDate()).endDate(savedSprint.getEndDate()).status(savedSprint.getStatus().name()).createdAt(savedSprint.getCreatedAt()).build();
    }

    @Transactional(readOnly = true)
    public List<SprintResponse> getSprintsWithIssues(Long projectId) {
        projectRepository.findById(projectId).filter(p -> !p.isDeleted()).orElseThrow(() -> new ProjectNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        List<SprintStatus> activeStatuses = List.of(SprintStatus.PENDING, SprintStatus.ACTIVE);
        List<Sprint> sprints = sprintRepository
                .findByProjectIdAndStatusInAndIsDeletedFalseOrderByCreatedAtAsc(projectId, activeStatuses);

        if (sprints.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> sprintIds = sprints.stream()
                .map(Sprint::getId)
                .collect(Collectors.toList());

        List<Issue> sprintIssues = issueRepository
                .findByProjectIdAndSprintIdInAndIsDeletedFalseOrderByPriorityDesc(projectId, sprintIds);

        Map<Long, List<Issue>> issuesBySprintId = sprintIssues.stream()
                .collect(Collectors.groupingBy(issue -> issue.getSprint().getId()));

        return sprints.stream().map(sprint -> {

            List<Issue> issuesInThisSprint = issuesBySprintId.getOrDefault(sprint.getId(), Collections.emptyList());

            List<IssueBacklogResponse> issueDTOs = issuesInThisSprint.stream()
                    .map(issue -> IssueBacklogResponse.builder()
                            .id(issue.getId())
                            .issueKey(issue.getIssueKey())
                            .issueType(issue.getIssueType().name())
                            .status(issue.getStatus().name())
                            .priority(issue.getPriority().name())
                            .createdAt(issue.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());

            return SprintResponse.builder()
                    .id(sprint.getId())
                    .projectId(sprint.getProjectId())
                    .name(sprint.getName())
                    .goal(sprint.getGoal())
                    .startDate(sprint.getStartDate())
                    .endDate(sprint.getEndDate())
                    .status(sprint.getStatus().name())
                    .createdAt(sprint.getCreatedAt())
                    .issues(issueDTOs)
                    .build();

        }).collect(Collectors.toList());
    }

    @Transactional
    public SprintResponse updateSprint(Long projectId, Long sprintId, Long userId, SprintUpdateRequest request) {
        projectRepository.findById(projectId).filter(p -> !p.isDeleted()).orElseThrow(() -> new ProjectNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        ProjectMember currentUserMember = projectMemberRepository.findByProjectIdAndUserIdAndIsDeletedFalse(projectId, userId)
                .orElseThrow(() -> new ForbiddenException("Bạn không phải là thành viên của dự án này"));

        if (ProjectRole.OWNER != currentUserMember.getProjectRole()
                && ProjectRole.MANAGER != currentUserMember.getProjectRole()) {
            throw new ForbiddenException("Chỉ OWNER hoặc MANAGER mới có quyền cập nhật Sprint");
        }

        Sprint sprint = sprintRepository.findByIdAndProjectIdAndIsDeletedFalse(sprintId, projectId)
                .orElseThrow(() -> new NotFoundException("Sprint không tồn tại hoặc đã bị xóa"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            sprint.setName(request.getName().trim());
        }
        if (request.getGoal() != null) {
            sprint.setGoal(request.getGoal());
        }
        if (request.getStartDate() != null) {
            sprint.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            sprint.setEndDate(request.getEndDate());
        }

        sprintRepository.save(sprint);

        return SprintResponse.builder()
                .id(sprint.getId())
                .projectId(sprint.getProjectId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .status(sprint.getStatus().name())
                .createdAt(sprint.getCreatedAt())
                .build();
    }

    @Transactional
    public void deleteSprint(Long projectId, Long sprintId, Long userId) {

        ProjectMember currentUserMember = projectMemberRepository.findByProjectIdAndUserIdAndIsDeletedFalse(projectId, userId)
                .orElseThrow(() -> new ForbiddenException("Bạn không phải là thành viên của dự án này"));

        if (ProjectRole.OWNER != currentUserMember.getProjectRole()
                && ProjectRole.MANAGER != currentUserMember.getProjectRole()) {
            throw new ForbiddenException("Chỉ OWNER hoặc MANAGER mới có quyền xóa Sprint");
        }

        Sprint sprint = sprintRepository.findByIdAndProjectIdAndIsDeletedFalse(sprintId, projectId)
                .orElseThrow(() -> new NotFoundException("Sprint không tồn tại hoặc đã bị xóa"));

        // if (sprint.getStatus() == SprintStatus.ACTIVE) {
        //     throw new BadRequestException("Không thể xóa Sprint đang trong quá trình chạy");
        // }

        sprint.setDeleted(true);
        sprintRepository.save(sprint);

        issueRepository.clearSprintIdForIssues(sprintId, projectId);
    }
}
