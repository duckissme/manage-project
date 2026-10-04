package com.qlda.manage_project.modules.timeline.service.impl;

import com.qlda.manage_project.common.exception.BadRequestException;
import com.qlda.manage_project.common.exception.ForbiddenException;
import com.qlda.manage_project.common.exception.NotFoundException;
import com.qlda.manage_project.modules.issue.entity.Issue;
import com.qlda.manage_project.modules.issue.enums.IssueLinkType;
import com.qlda.manage_project.modules.issue.event.IssueUpdatedEvent;
import com.qlda.manage_project.modules.issue.repository.IssueLinkRepository;
import com.qlda.manage_project.modules.issue.repository.IssueRepository;
import com.qlda.manage_project.modules.project.entity.ProjectMember;
import com.qlda.manage_project.modules.project.enums.ProjectRole;
import com.qlda.manage_project.modules.project.exception.ProjectNotFoundException;
import com.qlda.manage_project.modules.project.repository.ProjectMemberRepository;
import com.qlda.manage_project.modules.project.repository.ProjectRepository;
import com.qlda.manage_project.modules.sprint.repository.SprintRepository;
import com.qlda.manage_project.modules.timeline.converter.TimelineConverter;
import com.qlda.manage_project.modules.timeline.dto.request.IssueScheduleRequest;
import com.qlda.manage_project.modules.timeline.dto.response.TimelineDependencyResponse;
import com.qlda.manage_project.modules.timeline.dto.response.TimelineIssueResponse;
import com.qlda.manage_project.modules.timeline.dto.response.TimelineResponse;
import com.qlda.manage_project.modules.timeline.dto.response.TimelineSprintResponse;
import com.qlda.manage_project.modules.timeline.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimelineServiceImpl implements TimelineService {

    private final IssueRepository issueRepository;
    private final IssueLinkRepository issueLinkRepository;
    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TimelineConverter timelineConverter;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public TimelineResponse getTimeline(Long projectId, Long userId) {
        validateProject(projectId);
        getMemberOrThrow(projectId, userId);

        // Query 1: toàn bộ issue (kèm assignee)
        List<TimelineIssueResponse> issues = issueRepository.findTimelineIssues(projectId).stream()
                .map(timelineConverter::toIssueResponse)
                .collect(Collectors.toList());

        Set<Long> issueIds = issues.stream()
                .map(TimelineIssueResponse::getId)
                .collect(Collectors.toSet());

        // Issue con có cha đã bị xóa mềm -> đưa lên cấp gốc để FE không bị mất node khi dựng cây
        issues.forEach(issue -> {
            if (issue.getParentId() != null && !issueIds.contains(issue.getParentId())) {
                issue.setParentId(null);
            }
        });

        // Query 2: dependency BLOCKS, chỉ giữ link mà cả 2 đầu còn tồn tại
        List<TimelineDependencyResponse> dependencies = issueLinkRepository
                .findByProjectIdAndLinkType(projectId, IssueLinkType.BLOCKS).stream()
                .map(timelineConverter::toDependencyResponse)
                .filter(d -> issueIds.contains(d.getSourceIssueId()) && issueIds.contains(d.getTargetIssueId()))
                .collect(Collectors.toList());

        // Query 3: sprint markers
        List<TimelineSprintResponse> sprints = sprintRepository
                .findByProjectIdAndIsDeletedFalseOrderByCreatedAtAsc(projectId).stream()
                .map(timelineConverter::toSprintResponse)
                .collect(Collectors.toList());

        return TimelineResponse.builder()
                .issues(issues)
                .dependencies(dependencies)
                .sprints(sprints)
                .build();
    }

    @Override
    @Transactional
    public TimelineIssueResponse scheduleIssue(Long projectId, Long issueId, IssueScheduleRequest request, Long userId) {
        ProjectMember member = getMemberOrThrow(projectId, userId);
        if (member.getProjectRole() == ProjectRole.VIEWER) {
            throw new ForbiddenException("VIEWER không có quyền thay đổi lịch của Issue");
        }

        Issue issue = issueRepository.findByIdAndProjectIdAndIsDeletedFalse(issueId, projectId)
                .orElseThrow(() -> new NotFoundException("Issue không tồn tại hoặc đã bị xóa"));

        if (!Objects.equals(issue.getVersion(), request.getVersion())) {
            throw new BadRequestException("Dữ liệu đã bị thay đổi bởi người khác, vui lòng làm mới trang.");
        }

        if (request.getStartDate() != null && request.getDueDate() != null
                && request.getStartDate().isAfter(request.getDueDate())) {
            throw new BadRequestException("Ngày bắt đầu không được sau ngày kết thúc");
        }

        List<IssueUpdatedEvent.Change> changes = new ArrayList<>();

        if (!Objects.equals(issue.getStartDate(), request.getStartDate())) {
            changes.add(new IssueUpdatedEvent.Change("startDate",
                    Objects.toString(issue.getStartDate(), null),
                    Objects.toString(request.getStartDate(), null)));
            issue.setStartDate(request.getStartDate());
        }

        if (!Objects.equals(issue.getDueDate(), request.getDueDate())) {
            changes.add(new IssueUpdatedEvent.Change("dueDate",
                    Objects.toString(issue.getDueDate(), null),
                    Objects.toString(request.getDueDate(), null)));
            issue.setDueDate(request.getDueDate());
        }

        if (changes.isEmpty()) {
            return timelineConverter.toIssueResponse(issue);
        }

        // saveAndFlush để version mới được trả về ngay cho FE dùng ở lần kéo thả tiếp theo
        Issue saved = issueRepository.saveAndFlush(issue);
        eventPublisher.publishEvent(new IssueUpdatedEvent(issueId, userId, changes));

        return timelineConverter.toIssueResponse(saved);
    }

    private void validateProject(Long projectId) {
        projectRepository.findById(projectId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ProjectNotFoundException("Không tìm thấy dự án với ID: " + projectId));
    }

    private ProjectMember getMemberOrThrow(Long projectId, Long userId) {
        return projectMemberRepository.findByProjectIdAndUserIdAndIsDeletedFalse(projectId, userId)
                .orElseThrow(() -> new ForbiddenException("Bạn không phải thành viên của dự án này"));
    }
}
