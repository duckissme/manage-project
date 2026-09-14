package com.qlda.manage_project.modules.issue.entity;

import com.qlda.manage_project.modules.project.entity.Project;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "project_sequences")
@Getter
@Setter
public class ProjectSequence {

    @Id
    private Long projectId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    private Integer currentValue = 0;
}