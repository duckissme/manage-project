package com.qlda.manage_project.modules.backlog.service.impl;

import com.qlda.manage_project.modules.backlog.dto.response.IssueBacklogResponse;
import com.qlda.manage_project.modules.backlog.service.BacklogService;
import com.qlda.manage_project.modules.issue.entity.Issue;
import com.qlda.manage_project.modules.issue.repository.IssueRepository;
import com.qlda.manage_project.modules.project.entity.Project;
import com.qlda.manage_project.modules.project.exception.ProjectNotFoundException;
import com.qlda.manage_project.modules.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
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
    public List<IssueBacklogResponse> getBacklogIssues(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ProjectNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        List<Issue> backlogIssues = issueRepository
                .findByProjectIdAndSprintIsNullAndIsDeletedFalseOrderByCreatedAtDesc(projectId);

        return backlogIssues.stream().map(issue -> IssueBacklogResponse.builder()
                .id(issue.getId())
                .issueKey(issue.getIssueKey())
                .issueType(issue.getIssueType().name())
                .status(issue.getStatus().name())
                .priority(issue.getPriority().name())
                .createdAt(issue.getCreatedAt())
                .build()
        ).collect(Collectors.toList());
    }
}
