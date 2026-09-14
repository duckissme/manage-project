package com.qlda.manage_project.modules.issue.repository;

import com.qlda.manage_project.modules.issue.entity.IssueHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IssueHistoryRepository extends JpaRepository<IssueHistory, Long> {

    List<IssueHistory> findByIssueIdOrderByCreatedAtDesc(Long issueId);
}
