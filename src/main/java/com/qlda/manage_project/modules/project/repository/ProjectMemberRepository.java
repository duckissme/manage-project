package com.qlda.manage_project.modules.project.repository;

import com.qlda.manage_project.modules.project.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProjectIdAndIsDeletedFalse(Long projectId);

    Optional<ProjectMember> findByProjectIdAndUserIdAndIsDeletedFalse(Long projectId, Long userId);

    boolean existsByProjectIdAndUserIdAndIsDeletedFalse(Long projectId, Long userId);

    long countByProjectIdAndIsDeletedFalse(Long projectId);

    @Modifying
    @Query("UPDATE ProjectMember pm SET pm.isDeleted = true WHERE pm.id = :id")
    void deleteMember(@Param("id") Long id);
}
