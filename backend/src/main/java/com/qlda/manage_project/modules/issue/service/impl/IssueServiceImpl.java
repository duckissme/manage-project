package com.qlda.manage_project.modules.issue.service.impl;

import com.qlda.manage_project.common.exception.BadRequestException;
import com.qlda.manage_project.common.exception.ForbiddenException;
import com.qlda.manage_project.common.exception.NotFoundException;
import com.qlda.manage_project.modules.issue.converter.IssueConverter;
import com.qlda.manage_project.modules.issue.dto.request.IssueCreateRequest;
import com.qlda.manage_project.modules.issue.dto.request.IssueUpdateRequest;
import com.qlda.manage_project.modules.issue.dto.request.MoveIssueRequest;
import com.qlda.manage_project.modules.issue.dto.response.IssueResponse;
import com.qlda.manage_project.modules.issue.entity.Issue;
import com.qlda.manage_project.modules.issue.entity.ProjectSequence;
import com.qlda.manage_project.modules.issue.enums.IssueStatus;
import com.qlda.manage_project.modules.issue.event.IssueUpdatedEvent;
import com.qlda.manage_project.modules.issue.repository.IssueRepository;
import com.qlda.manage_project.modules.issue.repository.ProjectSequenceRepository;
import com.qlda.manage_project.modules.issue.service.IssueService;
import com.qlda.manage_project.modules.project.entity.ProjectMember;
import com.qlda.manage_project.modules.project.enums.ProjectRole;
import com.qlda.manage_project.modules.project.repository.ProjectMemberRepository;
import com.qlda.manage_project.modules.sprint.entity.Sprint;
import com.qlda.manage_project.modules.sprint.enums.SprintStatus;
import com.qlda.manage_project.modules.sprint.repository.SprintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {
    private final IssueRepository issueRepository;
    private final ProjectSequenceRepository sequenceRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SprintRepository sprintRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final IssueConverter issueConverter;

    @Transactional
    public IssueResponse createIssue(IssueCreateRequest request, Long reporterId, Long projectId, String projectKey) {

        ProjectSequence seq = sequenceRepository.findByProjectId(projectId).orElseThrow(() -> new NotFoundException("Không tìm thấy cấu hình project"));

        seq.setCurrentValue(seq.getCurrentValue() + 1);
        sequenceRepository.save(seq);

        String issueKey = projectKey + "-" + seq.getCurrentValue();

        Issue issue = new Issue();
        issue.setProjectId(projectId);
        issue.setIssueKey(issueKey);
        issue.setIssueType(request.getIssueType());
        issue.setTitle(request.getTitle());
        issue.setPriority(request.getPriority());
        issue.setDescription(request.getDescription());
        issue.setStoryPoint(request.getStoryPoint());
        issue.setAssigneeId(request.getAssigneeId());
        issue.setDueDate(request.getDueDate());
        issue.setReporterId(reporterId);
        issue.setStatus(IssueStatus.TO_DO);

        Issue savedIssue = issueRepository.save(issue);

        return issueConverter.mapToResponse(savedIssue);
    }

    @Transactional
    public IssueResponse updateIssue(Long issueId, IssueUpdateRequest request, Long actorId) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(() -> new NotFoundException("Không tìm thấy Issue"));

        List<IssueUpdatedEvent.Change> changes = new ArrayList<>();

        // So sánh Title
        if (!Objects.equals(issue.getTitle(), request.getTitle())) {
            changes.add(new IssueUpdatedEvent.Change("title", issue.getTitle(), request.getTitle()));
            issue.setTitle(request.getTitle());
        }

        // So sánh Status
        if (issue.getStatus() != request.getStatus()) {
            changes.add(new IssueUpdatedEvent.Change("status", issue.getStatus().name(), request.getStatus().name()));
            issue.setStatus(request.getStatus());
        }

        // So sánh Issue Type
        if (issue.getIssueType() != request.getIssueType()) {
            changes.add(new IssueUpdatedEvent.Change("issueType", issue.getIssueType().name(), request.getIssueType().name()));
            issue.setIssueType(request.getIssueType());
        }

        // So sánh Priority
        if (issue.getPriority() != request.getPriority()) {
            changes.add(new IssueUpdatedEvent.Change("priority", issue.getPriority().name(), request.getPriority().name()));
            issue.setPriority(request.getPriority());
        }

        // So sánh Description
        if (!Objects.equals(issue.getDescription(), request.getDescription())) {
            changes.add(new IssueUpdatedEvent.Change("description", issue.getDescription(), request.getDescription()));
            issue.setDescription(request.getDescription());
        }

        // So sánh Assignee
        if (!Objects.equals(issue.getAssigneeId(), request.getAssigneeId())) {
            changes.add(new IssueUpdatedEvent.Change("assigneeId", Objects.toString(issue.getAssigneeId(), null), Objects.toString(request.getAssigneeId(), null)));
            issue.setAssigneeId(request.getAssigneeId());
        }

        // So sánh Story Point
        if (!Objects.equals(issue.getStoryPoint(), request.getStoryPoint())) {
            changes.add(new IssueUpdatedEvent.Change("storyPoint", Objects.toString(issue.getStoryPoint(), null), Objects.toString(request.getStoryPoint(), null)));
            issue.setStoryPoint(request.getStoryPoint());
        }

        // So sánh Due Date
        if (!Objects.equals(issue.getDueDate(), request.getDueDate())) {
            changes.add(new IssueUpdatedEvent.Change("dueDate", Objects.toString(issue.getDueDate(), null), Objects.toString(request.getDueDate(), null)));
            issue.setDueDate(request.getDueDate());
        }

        Issue updatedIssue = issueRepository.save(issue);

        if (!changes.isEmpty()) {
            eventPublisher.publishEvent(new IssueUpdatedEvent(issueId, actorId, changes));
        }

        return issueConverter.mapToResponse(updatedIssue);
    }

    @Transactional
    public void deleteIssue(Long issueId, Long actorId) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(() -> new NotFoundException("Không tìm thấy Issue"));

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserIdAndIsDeletedFalse(issue.getProjectId(), actorId)
                .orElseThrow(() -> new NotFoundException("Thành viên này không tồn tại trong dự án"));

        if (ProjectRole.OWNER != member.getProjectRole()
                && ProjectRole.MANAGER != member.getProjectRole()) {
            throw new ForbiddenException("Chỉ OWNER hoặc MANAGER mới có quyền xóa Issue");
        }

        int updatedRows = issueRepository.softDeleteByIdAndVersion(issue.getId(), issue.getVersion());

        if (updatedRows == 0) {
            throw new ObjectOptimisticLockingFailureException(Issue.class, issueId);
        }

        IssueUpdatedEvent.Change deleteChange = new IssueUpdatedEvent.Change(
                "Status",
                "Active",
                "Deleted"
        );

        eventPublisher.publishEvent(new IssueUpdatedEvent(
                issue.getId(),
                actorId,
                List.of(deleteChange)
        ));
    }

    @Transactional(readOnly = true)
    public IssueResponse viewDetailIssue(Long issueId) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(() -> new NotFoundException("Không tìm thấy Issue"));

        return issueConverter.mapToResponse(issue);
    }

    @Transactional
    public void moveIssue(Long projectId, Long issueId, MoveIssueRequest request, Long actorId) {

        Issue issue = issueRepository.findByIdAndProjectIdAndIsDeletedFalse(issueId, projectId)
                .orElseThrow(() -> new NotFoundException("Issue không tồn tại hoặc đã bị xóa"));

        // 2. Kiểm tra Optimistic Locking (version)
        if (!issue.getVersion().equals(request.getVersion())) {
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

            String oldValue = (oldSprint != null) ? oldSprint.getName() : "Backlog";
            String newValue = (targetSprint != null) ? targetSprint.getName() : "Backlog";

            IssueUpdatedEvent.Change sprintChange = new IssueUpdatedEvent.Change(
                    "Sprint", oldValue, newValue
            );

            eventPublisher.publishEvent(new IssueUpdatedEvent(
                    issue.getId(),
                    actorId,
                    List.of(sprintChange)
            ));
        }
    }
}
