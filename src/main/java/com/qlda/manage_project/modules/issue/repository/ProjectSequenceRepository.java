package com.qlda.manage_project.modules.issue.repository;

import com.qlda.manage_project.modules.issue.entity.ProjectSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProjectSequenceRepository extends JpaRepository<ProjectSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProjectSequence> findByProjectId(Long projectId);
}
