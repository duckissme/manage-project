package com.qlda.manage_project.modules.issue.entity;

import com.qlda.manage_project.modules.issue.enums.IssuePriority;
import com.qlda.manage_project.modules.issue.enums.IssueStatus;
import com.qlda.manage_project.modules.issue.enums.IssueType;
import com.qlda.manage_project.modules.project.entity.Project;
import com.qlda.manage_project.modules.sprint.entity.Sprint;
import com.qlda.manage_project.modules.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "issues")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("is_deleted = false")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", nullable = true)
    private Sprint sprint;

    @Column(name = "issue_key", nullable = false, unique = true)
    private String issueKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false)
    private IssueType issueType;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IssueStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuePriority priority = IssuePriority.MEDIUM;

    @Column(name = "story_point")
    private Integer storyPoint;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", insertable = false, updatable = false)
    private UserEntity reporter;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id", insertable = false, updatable = false)
    private UserEntity assignee;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Version
    private Integer version;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
