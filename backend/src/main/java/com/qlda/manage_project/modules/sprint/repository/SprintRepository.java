package com.qlda.manage_project.modules.sprint.repository;

import com.qlda.manage_project.modules.sprint.entity.Sprint;
import com.qlda.manage_project.modules.sprint.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {
    long countByProjectId(Long projectId);

    List<Sprint> findByProjectIdAndIsDeletedFalseOrderByCreatedAtAsc(Long projectId);

    List<Sprint> findByProjectIdAndStatusInAndIsDeletedFalseOrderByCreatedAtAsc(
            Long projectId, List<SprintStatus> statuses);

    Optional<Sprint> findByIdAndProjectIdAndIsDeletedFalse(Long id, Long projectId);

    boolean existsByProjectIdAndStatusAndIsDeletedFalse(Long projectId, SprintStatus status);
}
