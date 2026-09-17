package com.qlda.manage_project.modules.issue.repository;

import com.qlda.manage_project.modules.issue.entity.IssueLink;
import com.qlda.manage_project.modules.issue.enums.IssueLinkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueLinkRepository extends JpaRepository<IssueLink, Long> {

    @Query("SELECT il FROM IssueLink il " +
           "JOIN FETCH il.sourceIssue s " +
           "JOIN FETCH il.targetIssue t " +
           "WHERE il.sourceIssue.id = :issueId OR il.targetIssue.id = :issueId")
    List<IssueLink> findAllLinksByIssueId(@Param("issueId") Long issueId);

    boolean existsBySourceIssueIdAndTargetIssueIdAndLinkType(
            Long sourceIssueId,
            Long targetIssueId,
            IssueLinkType linkType
    );

    boolean existsBySourceIssueIdAndTargetIssueId(Long sourceIssueId, Long targetIssueId);
}
