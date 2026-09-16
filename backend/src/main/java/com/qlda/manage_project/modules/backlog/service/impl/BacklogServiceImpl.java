package com.qlda.manage_project.modules.backlog.service.impl;

import com.qlda.manage_project.modules.backlog.dto.response.IssueBacklogResponse;
import com.qlda.manage_project.modules.backlog.service.BacklogService;
import com.qlda.manage_project.modules.issue.entity.Issue;
import com.qlda.manage_project.modules.issue.enums.IssueStatus;
import com.qlda.manage_project.modules.issue.enums.IssueType;
import com.qlda.manage_project.modules.issue.repository.IssueRepository;
import com.qlda.manage_project.modules.issue.specification.IssueSpecification;
import com.qlda.manage_project.modules.project.exception.ProjectNotFoundException;
import com.qlda.manage_project.modules.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BacklogServiceImpl implements BacklogService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    @Override
    public List<IssueBacklogResponse> getBacklogIssues(
            Long projectId,
            IssueType issueType,
            Long assigneeId,
            IssueStatus issueStatus,
            String searchKeyword) {

        projectRepository.findById(projectId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ProjectNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        Specification<Issue> spec = IssueSpecification.filterBacklog(
                projectId, issueType, assigneeId, issueStatus, searchKeyword
        );

        List<Issue> backlogIssues = issueRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));

        return backlogIssues.stream().map(issue -> IssueBacklogResponse.builder()
                .id(issue.getId())
                .issueKey(issue.getIssueKey())
                .title(issue.getTitle())
                .issueType(issue.getIssueType().name())
                .status(issue.getStatus().name())
                .priority(issue.getPriority().name())
                .parentId(issue.getParent().getId())
                .createdAt(issue.getCreatedAt())
                .build()
        ).collect(Collectors.toList());
    }
}
