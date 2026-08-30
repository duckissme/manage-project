package com.qlda.manage_project.modules.issue.repository;

import com.qlda.manage_project.modules.issue.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByProjectIdAndSprintIsNullAndIsDeletedFalseOrderByCreatedAtDesc(Long projectId);

    @Modifying
    @Query("UPDATE Issue i SET i.isDeleted = true, i.version = i.version + 1 WHERE i.id = :id AND i.version = :version")
    int softDeleteByIdAndVersion(@Param("id") Long id, @Param("version") Integer version);
}
