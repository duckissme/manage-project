package com.qlda.manage_project.modules.issue.repository;

import com.qlda.manage_project.modules.issue.entity.Issue;
import com.qlda.manage_project.modules.issue.enums.IssueType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long>, JpaSpecificationExecutor<Issue> {
    List<Issue> findByProjectIdAndSprintIsNullAndIsDeletedFalseOrderByCreatedAtDesc(Long projectId);

    @Modifying
    @Query("UPDATE Issue i SET i.isDeleted = true, i.version = i.version + 1 WHERE i.id = :id AND i.version = :version")
    int softDeleteByIdAndVersion(@Param("id") Long id, @Param("version") Integer version);

    List<Issue> findByProjectIdAndSprintIdInAndIsDeletedFalseOrderByPriorityDesc(
            Long projectId, List<Long> sprintIds);

    List<Issue> findByProjectIdAndSprintIdAndIsDeletedFalse(
            Long projectId, Long sprintId);

    boolean existsByProjectIdAndSprintIdAndIsDeletedFalse(Long projectId, Long sprintId);

    @Modifying
    @Query("UPDATE Issue i SET i.sprint = null WHERE i.sprint.id = :sprintId AND i.project.id = :projectId")
    void clearSprintIdForIssues(@Param("sprintId") Long sprintId, @Param("projectId") Long projectId);

    Optional<Issue> findByIdAndProjectIdAndIsDeletedFalse(Long id, Long projectId);

    List<Issue> findByParentId(Long parentId);

    Optional<Issue> findByIdAndIsDeletedFalse(Long id);

    List<Issue> findByProjectIdAndIsDeletedFalse(Long projectId);

    List<Issue> findByProjectIdAndIssueTypeAndIsDeletedFalse(Long projectId, IssueType issueType);
}
