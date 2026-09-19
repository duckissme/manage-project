package com.qlda.manage_project.modules.issue.service.impl;

import com.qlda.manage_project.common.exception.BadRequestException;
import com.qlda.manage_project.common.exception.ForbiddenException;
import com.qlda.manage_project.common.exception.NotFoundException;
import com.qlda.manage_project.modules.issue.converter.IssueConverter;
import com.qlda.manage_project.modules.issue.dto.request.IssueCreateRequest;
import com.qlda.manage_project.modules.issue.dto.request.IssueUpdateRequest;
import com.qlda.manage_project.modules.issue.dto.request.MoveIssueRequest;
import com.qlda.manage_project.modules.issue.dto.response.IssueResponse;
import com.qlda.manage_project.modules.issue.dto.response.IssueSummaryResponse;
import com.qlda.manage_project.modules.issue.entity.Issue;
import com.qlda.manage_project.modules.issue.entity.ProjectSequence;
import com.qlda.manage_project.modules.issue.enums.IssuePriority;
import com.qlda.manage_project.modules.issue.enums.IssueStatus;
import com.qlda.manage_project.modules.issue.enums.IssueType;
import com.qlda.manage_project.modules.issue.event.IssueUpdatedEvent;
import com.qlda.manage_project.modules.issue.repository.IssueRepository;
import com.qlda.manage_project.modules.issue.repository.ProjectSequenceRepository;
import com.qlda.manage_project.modules.issue.service.IssueLinkService;
import com.qlda.manage_project.modules.issue.service.IssueService;
import com.qlda.manage_project.modules.issue.specification.IssueSpecification;
import com.qlda.manage_project.modules.project.entity.Project;
import com.qlda.manage_project.modules.project.entity.ProjectMember;
import com.qlda.manage_project.modules.project.enums.ProjectRole;
import com.qlda.manage_project.modules.project.exception.ProjectNotFoundException;
import com.qlda.manage_project.modules.project.repository.ProjectMemberRepository;
import com.qlda.manage_project.modules.project.repository.ProjectRepository;
import com.qlda.manage_project.modules.sprint.entity.Sprint;
import com.qlda.manage_project.modules.sprint.enums.SprintStatus;
import com.qlda.manage_project.modules.sprint.repository.SprintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {
    private final IssueRepository issueRepository;
    private final ProjectSequenceRepository sequenceRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final IssueConverter issueConverter;
    private final IssueLinkService issueLinkService;

    @Transactional
    public IssueSummaryResponse createIssue(IssueCreateRequest request, Long reporterId, Long projectId) {
        List<IssueSummaryResponse> responses = this.createBulk(List.of(request), reporterId, projectId);

        return responses.get(0);
    }

    @Transactional
    public List<IssueSummaryResponse> createBulk(List<IssueCreateRequest> requests, Long reporterId, Long projectId) {

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy project"));
        ProjectSequence seq = sequenceRepository.findByProjectId(projectId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy cấu hình project"));

        String projectKey = project.getProjectKey();
        List<Issue> savedIssues = new ArrayList<>();

        for (IssueCreateRequest request : requests) {

            seq.setCurrentValue(seq.getCurrentValue() + 1);
            String issueKey = projectKey + "-" + seq.getCurrentValue();

            Issue issue = new Issue();
            issue.setProject(project);
            issue.setIssueKey(issueKey);
            issue.setIssueType(request.getIssueType() != null ? request.getIssueType() : IssueType.USER_STORY);
            issue.setTitle(request.getTitle());
            issue.setPriority(request.getPriority() != null ? request.getPriority() : IssuePriority.MEDIUM);
            issue.setDescription(request.getDescription());
            issue.setAssigneeId(request.getAssigneeId());
            issue.setDueDate(request.getDueDate());
            issue.setReporterId(reporterId);
            issue.setStatus(IssueStatus.TO_DO);

            if (request.getParentId() != null) {
                Issue parent = issueRepository.findByIdAndProjectIdAndIsDeletedFalse(request.getParentId(), projectId)
                        .orElseThrow(() -> new NotFoundException("Không tìm thấy Issue cha với ID: " + request.getParentId()));
                issue.setParent(parent);
                if (issue.getIssueType() == IssueType.SUB_TASK && parent.getSprint() != null) {
                    issue.setSprint(parent.getSprint());
                }
            }

            Issue savedIssue = issueRepository.save(issue);
            savedIssues.add(savedIssue);
        }

        sequenceRepository.save(seq);

        return savedIssues.stream()
                .map(issueConverter::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IssueResponse updateIssue(Long issueId, IssueUpdateRequest request, Long actorId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy Issue"));

        // Kiểm tra Optimistic Locking (version)
        if (!Objects.equals(issue.getVersion(), request.getVersion())) {
            throw new BadRequestException("Dữ liệu đã bị thay đổi bởi người khác, vui lòng làm mới trang.");
        }

        List<IssueUpdatedEvent.Change> changes = new ArrayList<>();

        // So sánh Title
        if (!Objects.equals(issue.getTitle(), request.getTitle())) {
            changes.add(new IssueUpdatedEvent.Change("title", issue.getTitle(), request.getTitle()));
            issue.setTitle(request.getTitle());
        }

        // So sánh Status
        if (request.getStatus() != null && issue.getStatus() != request.getStatus()) {
            changes.add(new IssueUpdatedEvent.Change("status", issue.getStatus().name(), request.getStatus().name()));
            issue.setStatus(request.getStatus());
        }

        // So sánh Issue Type
        if (request.getIssueType() != null && issue.getIssueType() != request.getIssueType()) {
            changes.add(new IssueUpdatedEvent.Change("issueType", issue.getIssueType().name(),
                    request.getIssueType().name()));
            issue.setIssueType(request.getIssueType());
        }

        // So sánh Priority
        if (request.getPriority() != null && issue.getPriority() != request.getPriority()) {
            changes.add(
                    new IssueUpdatedEvent.Change("priority", issue.getPriority().name(), request.getPriority().name()));
            issue.setPriority(request.getPriority());
        }

        // So sánh Description
        if (!Objects.equals(issue.getDescription(), request.getDescription())) {
            changes.add(new IssueUpdatedEvent.Change("description", issue.getDescription(), request.getDescription()));
            issue.setDescription(request.getDescription());
        }

        // So sánh Assignee
        if (!Objects.equals(issue.getAssigneeId(), request.getAssigneeId())) {
            changes.add(new IssueUpdatedEvent.Change("assigneeId", Objects.toString(issue.getAssigneeId(), null),
                    Objects.toString(request.getAssigneeId(), null)));
            issue.setAssigneeId(request.getAssigneeId());
        }

        // So sánh Story Point
        if (!Objects.equals(issue.getStoryPoint(), request.getStoryPoint())) {
            changes.add(new IssueUpdatedEvent.Change("storyPoint", Objects.toString(issue.getStoryPoint(), null),
                    Objects.toString(request.getStoryPoint(), null)));
            issue.setStoryPoint(request.getStoryPoint());
        }

        // So sánh Due Date
        if (!Objects.equals(issue.getDueDate(), request.getDueDate())) {
            changes.add(new IssueUpdatedEvent.Change("dueDate", Objects.toString(issue.getDueDate(), null),
                    Objects.toString(request.getDueDate(), null)));
            issue.setDueDate(request.getDueDate());
        }

        // So sánh Parent Issue
        Long oldParentId = issue.getParent() != null ? issue.getParent().getId() : null;
        if (!Objects.equals(oldParentId, request.getParentId())) {
            if (request.getParentId() != null) {
                if (request.getParentId().equals(issue.getId())) {
                    throw new BadRequestException("Issue không thể làm cha của chính nó");
                }
                Issue parentIssue = issueRepository.findByIdAndProjectIdAndIsDeletedFalse(request.getParentId(), issue.getProject().getId())
                        .orElseThrow(() -> new NotFoundException("Không tìm thấy Issue cha với ID: " + request.getParentId()));

                issue.setParent(parentIssue);
            } else {
                issue.setParent(null);
            }

            changes.add(new IssueUpdatedEvent.Change("parentId", Objects.toString(oldParentId, null),
                    Objects.toString(request.getParentId(), null)));
        }

        Issue updatedIssue = issueRepository.save(issue);

        if (!changes.isEmpty()) {
            eventPublisher.publishEvent(new IssueUpdatedEvent(issueId, actorId, changes));
        }

        IssueResponse response = issueConverter.mapToResponse(updatedIssue);
        response.setLinks(issueLinkService.getLinksByIssue(issueId));
        return response;
    }

    @Transactional
    public void deleteIssue(Long issueId, Long actorId, Integer version) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy Issue"));

        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserIdAndIsDeletedFalse(issue.getProject().getId(), actorId)
                .orElseThrow(() -> new NotFoundException("Thành viên này không tồn tại trong dự án"));

        if (ProjectRole.OWNER != member.getProjectRole()
                && ProjectRole.MANAGER != member.getProjectRole()) {
            throw new ForbiddenException("Chỉ OWNER hoặc MANAGER mới có quyền xóa Issue");
        }

        // Kiểm tra version nếu client truyền lên
        if (version != null && !Objects.equals(issue.getVersion(), version)) {
            throw new BadRequestException("Dữ liệu đã bị thay đổi bởi người khác, vui lòng làm mới trang.");
        }

        int updatedRows = issueRepository.softDeleteByIdAndVersion(issue.getId(), issue.getVersion());

        if (updatedRows == 0) {
            throw new ObjectOptimisticLockingFailureException(Issue.class, issueId);
        }

        IssueUpdatedEvent.Change deleteChange = new IssueUpdatedEvent.Change(
                "Status",
                "Active",
                "Deleted");

        eventPublisher.publishEvent(new IssueUpdatedEvent(
                issue.getId(),
                actorId,
                List.of(deleteChange)));
    }

    @Transactional(readOnly = true)
    public IssueResponse viewDetailIssue(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy Issue"));

        IssueResponse response = issueConverter.mapToResponse(issue);
        response.setLinks(issueLinkService.getLinksByIssue(issueId));
        return response;
    }

    @Transactional
    public void moveIssue(Long projectId, Long issueId, MoveIssueRequest request, Long actorId) {

        Issue issue = issueRepository.findByIdAndProjectIdAndIsDeletedFalse(issueId, projectId)
                .orElseThrow(() -> new NotFoundException("Issue không tồn tại hoặc đã bị xóa"));

        if (!Objects.equals(issue.getVersion(), request.getVersion())) {
            throw new BadRequestException("Dữ liệu đã bị thay đổi bởi người khác, vui lòng làm mới trang.");
        }

        Sprint oldSprint = issue.getSprint();
        Long oldSprintId = (oldSprint != null) ? oldSprint.getId() : null;
        Long targetSprintId = request.getTargetSprintId();

        if (!Objects.equals(oldSprintId, targetSprintId)) {

            Sprint targetSprint = null;
            if (targetSprintId != null) {
                targetSprint = sprintRepository.findByIdAndProjectIdAndIsDeletedFalse(targetSprintId, projectId)
                        .orElseThrow(() -> new NotFoundException("Sprint đích không tồn tại"));

                // Chặn không cho kéo Issue vào Sprint đã COMPLETED
                if (targetSprint.getStatus() == SprintStatus.COMPLETED) {
                    throw new BadRequestException("Không thể kéo Issue vào Sprint đã kết thúc");
                }
            }

            issue.setSprint(targetSprint);

            issueRepository.save(issue);

            // Đồng bộ sprint cho các subtask con theo Issue cha
            List<Issue> childIssues = issueRepository.findByParentId(issue.getId());
            if (!childIssues.isEmpty()) {
                for (Issue child : childIssues) {
                    if (child.getIssueType() == IssueType.SUB_TASK) {
                        child.setSprint(targetSprint);
                    }
                }
                issueRepository.saveAll(childIssues);
            }

            String oldValue = (oldSprint != null) ? oldSprint.getName() : "Backlog";
            String newValue = (targetSprint != null) ? targetSprint.getName() : "Backlog";

            IssueUpdatedEvent.Change sprintChange = new IssueUpdatedEvent.Change(
                    "Sprint", oldValue, newValue);

            eventPublisher.publishEvent(new IssueUpdatedEvent(
                    issue.getId(),
                    actorId,
                    List.of(sprintChange)));
        }
    }

    @Transactional(readOnly = true)
    public Long getProjectIdByIssueId(Long issueId) {
        Issue issue = issueRepository.findByIdAndIsDeletedFalse(issueId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy Issue"));

        return issue.getProject().getId();
    }

    @Transactional(readOnly = true)
    public List<IssueSummaryResponse> getChildIssues(Long parentId) {
        List<Issue> childIssues = issueRepository.findByParentId(parentId);

        return childIssues.stream()
                .map(issueConverter::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<IssueSummaryResponse> getIssues(
            Long projectId,
            IssueType type,
            String keyword,
            Long excludeIssueId,
            Integer limit) {

        projectRepository.findById(projectId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ProjectNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        Specification<Issue> spec = IssueSpecification.filterProjectIssues(projectId, type, keyword, excludeIssueId);

        List<Issue> issues;
        if (limit != null && limit > 0) {
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
            issues = issueRepository.findAll(spec, pageable).getContent();
        } else {
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
            issues = issueRepository.findAll(spec, sort);
        }

        return issues.stream()
                .map(issueConverter::mapToSummaryResponse)
                .collect(Collectors.toList());
    }
}
