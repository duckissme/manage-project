package com.qlda.manage_project.modules.sprint.service.impl;

import com.qlda.manage_project.common.exception.BadRequestException;
import com.qlda.manage_project.common.exception.ForbiddenException;
import com.qlda.manage_project.common.exception.NotFoundException;
import com.qlda.manage_project.modules.backlog.dto.response.IssueBacklogResponse;
import com.qlda.manage_project.modules.issue.entity.Issue;
import com.qlda.manage_project.modules.issue.enums.IssueStatus;
import com.qlda.manage_project.modules.issue.enums.IssueType;
import com.qlda.manage_project.modules.issue.event.IssueUpdatedEvent;
import com.qlda.manage_project.modules.issue.repository.IssueRepository;
import com.qlda.manage_project.modules.issue.specification.IssueSpecification;
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
import com.qlda.manage_project.modules.sprint.event.SprintStartedEvent;
import com.qlda.manage_project.modules.sprint.repository.SprintRepository;
import com.qlda.manage_project.modules.sprint.service.SprintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SprintResponse createSprint(Long projectId, SprintCreateRequest request) {
        projectRepository.findById(projectId).filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ProjectNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        String sprintName = request.getName();
        if (sprintName == null || sprintName.trim().isEmpty()) {
            long totalSprints = sprintRepository.countByProjectId(projectId);
            sprintName = "Sprint " + (totalSprints + 1);
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

        return SprintResponse.builder().id(savedSprint.getId()).projectId(savedSprint.getProjectId())
                .name(savedSprint.getName()).goal(savedSprint.getGoal()).startDate(savedSprint.getStartDate())
                .endDate(savedSprint.getEndDate()).status(savedSprint.getStatus().name())
                .createdAt(savedSprint.getCreatedAt()).build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<SprintResponse> getSprintsWithIssues(
            Long projectId,
            IssueType issueType,
            Long assigneeId,
            IssueStatus issueStatus,
            String searchKeyword) {
        projectRepository.findById(projectId).filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ProjectNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        List<SprintStatus> activeStatuses = List.of(SprintStatus.PENDING, SprintStatus.ACTIVE);
        List<Sprint> sprints = sprintRepository
                .findByProjectIdAndStatusInAndIsDeletedFalseOrderByCreatedAtAsc(projectId, activeStatuses);

        if (sprints.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> sprintIds = sprints.stream()
                .map(Sprint::getId)
                .collect(Collectors.toList());

        Specification<Issue> spec = IssueSpecification.filterSprintIssues(
                projectId, sprintIds, issueType, assigneeId, issueStatus, searchKeyword
        );

        List<Issue> sprintIssues = issueRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "priority"));

        Map<Long, List<Issue>> issuesBySprintId = sprintIssues.stream()
                .filter(issue -> issue.getSprint() != null)
                .collect(Collectors.groupingBy(issue -> issue.getSprint().getId()));

        return sprints.stream().map(sprint -> {

            List<Issue> issuesInThisSprint = issuesBySprintId.getOrDefault(sprint.getId(), Collections.emptyList());

            int totalTasks = issuesInThisSprint.size();
            int totalStoryPoints = issuesInThisSprint.stream()
                    .mapToInt(issue -> issue.getStoryPoint() != null ? issue.getStoryPoint() : 0)
                    .sum();
            int toDoPoints = issuesInThisSprint.stream()
                    .filter(issue -> issue.getStatus() == IssueStatus.TO_DO)
                    .mapToInt(issue -> issue.getStoryPoint() != null ? issue.getStoryPoint() : 0)
                    .sum();
            int inProgressPoints = issuesInThisSprint.stream()
                    .filter(issue -> issue.getStatus() == IssueStatus.IN_PROGRESS)
                    .mapToInt(issue -> issue.getStoryPoint() != null ? issue.getStoryPoint() : 0)
                    .sum();
            int donePoints = issuesInThisSprint.stream()
                    .filter(issue -> issue.getStatus() == IssueStatus.DONE)
                    .mapToInt(issue -> issue.getStoryPoint() != null ? issue.getStoryPoint() : 0)
                    .sum();

            List<IssueBacklogResponse> issueDTOs = issuesInThisSprint.stream()
                    .map(issue -> IssueBacklogResponse.builder()
                            .id(issue.getId())
                            .issueKey(issue.getIssueKey())
                            .title(issue.getTitle())
                            .issueType(issue.getIssueType().name())
                            .status(issue.getStatus().name())
                            .priority(issue.getPriority().name())
                            .parentId(issue.getParent() != null ? issue.getParent().getId() : null)
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
                    .totalTasks(totalTasks)
                    .totalStoryPoints(totalStoryPoints)
                    .toDoPoints(toDoPoints)
                    .inProgressPoints(inProgressPoints)
                    .donePoints(donePoints)
                    .issues(issueDTOs)
                    .build();

        }).collect(Collectors.toList());
    }

    @Transactional
    public SprintResponse updateSprint(Long projectId, Long sprintId, Long userId, SprintUpdateRequest request) {
        projectRepository.findById(projectId).filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ProjectNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        ProjectMember currentUserMember = projectMemberRepository
                .findByProjectIdAndUserIdAndIsDeletedFalse(projectId, userId)
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

        ProjectMember currentUserMember = projectMemberRepository
                .findByProjectIdAndUserIdAndIsDeletedFalse(projectId, userId)
                .orElseThrow(() -> new ForbiddenException("Bạn không phải là thành viên của dự án này"));

        if (ProjectRole.OWNER != currentUserMember.getProjectRole()
                && ProjectRole.MANAGER != currentUserMember.getProjectRole()) {
            throw new ForbiddenException("Chỉ OWNER hoặc MANAGER mới có quyền xóa Sprint");
        }

        Sprint sprint = sprintRepository.findByIdAndProjectIdAndIsDeletedFalse(sprintId, projectId)
                .orElseThrow(() -> new NotFoundException("Sprint không tồn tại hoặc đã bị xóa"));

        // if (sprint.getStatus() == SprintStatus.ACTIVE) {
        // throw new BadRequestException("Không thể xóa Sprint đang trong quá trình
        // chạy");
        // }

        sprint.setDeleted(true);
        sprintRepository.save(sprint);

        List<Issue> issuesInSprint = issueRepository.findByProjectIdAndSprintIdAndIsDeletedFalse(projectId, sprintId);

        issueRepository.clearSprintIdForIssues(sprintId, projectId);

        String oldSprintName = sprint.getName();
        String newSprintName = "Backlog";

        for (Issue issue : issuesInSprint) {
            IssueUpdatedEvent.Change change = new IssueUpdatedEvent.Change(
                    "Sprint", oldSprintName, newSprintName);

            eventPublisher.publishEvent(new IssueUpdatedEvent(
                    issue.getId(),
                    userId,
                    List.of(change)));
        }
    }

    @Transactional
    @Override
    public SprintResponse startSprint(Long projectId, Long sprintId, Long userId) {
        Sprint sprint = sprintRepository.findByIdAndProjectIdAndIsDeletedFalse(sprintId, projectId)
                .orElseThrow(() -> new NotFoundException("Sprint không tồn tại hoặc đã bị xóa"));

        if (sprint.getStatus() != SprintStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể bắt đầu Sprint khi đang ở trạng thái PENDING");
        }

        if (sprintRepository.existsByProjectIdAndStatusAndIsDeletedFalse(projectId, SprintStatus.ACTIVE)) {
            throw new BadRequestException("Dự án đã có một Sprint đang ở trạng thái ACTIVE");
        }

        if (!issueRepository.existsByProjectIdAndSprintIdAndIsDeletedFalse(projectId, sprintId)) {
            throw new BadRequestException("Không thể bắt đầu Sprint khi chưa có issue nào");
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        eventPublisher.publishEvent(new SprintStartedEvent(
                projectId, sprint.getId(), sprint.getName(), userId));

        Sprint savedSprint = sprintRepository.save(sprint);

        return SprintResponse.builder()
                .id(savedSprint.getId())
                .projectId(savedSprint.getProjectId())
                .name(savedSprint.getName())
                .goal(savedSprint.getGoal())
                .startDate(savedSprint.getStartDate())
                .endDate(savedSprint.getEndDate())
                .status(savedSprint.getStatus().name())
                .createdAt(savedSprint.getCreatedAt())
                .build();
    }

    @Transactional
    @Override
    public SprintResponse completeSprint(Long projectId, Long sprintId, Long userId) {
        Sprint sprint = sprintRepository.findByIdAndProjectIdAndIsDeletedFalse(sprintId, projectId)
                .orElseThrow(() -> new NotFoundException("Sprint không tồn tại hoặc đã bị xóa"));
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new BadRequestException("Chỉ có thể kết thúc Sprint khi đang ở trạng thái ACTIVE");
        }
        List<Issue> issuesInSprint = issueRepository.findByProjectIdAndSprintIdAndIsDeletedFalse(projectId, sprintId);

        List<Issue> incompleteIssues = issuesInSprint.stream()
                .filter(issue -> issue.getStatus() != IssueStatus.DONE)
                .collect(Collectors.toList());
        if (!incompleteIssues.isEmpty()) {
            String sprintName = sprint.getName();
            for (Issue issue : incompleteIssues) {
                issue.setSprint(null);
                IssueUpdatedEvent.Change change = new IssueUpdatedEvent.Change(
                        "Sprint", sprintName, "Backlog (Sprint kết thúc)");
                eventPublisher.publishEvent(new IssueUpdatedEvent(
                        issue.getId(),
                        userId,
                        List.of(change)));
            }
            issueRepository.saveAll(incompleteIssues);
        }
        sprint.setStatus(SprintStatus.COMPLETED);
        Sprint savedSprint = sprintRepository.save(sprint);
        return SprintResponse.builder()
                .id(savedSprint.getId())
                .projectId(savedSprint.getProjectId())
                .name(savedSprint.getName())
                .goal(savedSprint.getGoal())
                .startDate(savedSprint.getStartDate())
                .endDate(savedSprint.getEndDate())
                .status(savedSprint.getStatus().name())
                .createdAt(savedSprint.getCreatedAt())
                .build();
    }
}
