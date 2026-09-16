package com.qlda.manage_project.modules.project.repository;

import com.qlda.manage_project.modules.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("SELECT DISTINCT p FROM Project p JOIN p.members pm " +
            "WHERE pm.user.id = :userId " +
            "AND p.isDeleted = false " +
            "AND pm.isDeleted = false")
    List<Project> findActiveProjectsByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT p FROM Project p JOIN p.members pm " +
            "WHERE pm.user.id = :userId " +
            "AND p.isDeleted = false " +
            "AND pm.isDeleted = false")
    Page<Project> findActiveProjectsByUserId(@Param("userId") Long userId, Pageable pageable);

    List<Project> findByNameContainingIgnoreCase(String name);

    @Modifying
    @Query("UPDATE Project p SET p.isDeleted = true WHERE p.id = :id")
    void deleteProject(@Param("id") Long id);

    boolean existsByProjectKey(String projectKey);

}
