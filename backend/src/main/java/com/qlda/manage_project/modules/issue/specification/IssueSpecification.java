package com.qlda.manage_project.modules.issue.specification;

import com.qlda.manage_project.modules.issue.entity.Issue;
import com.qlda.manage_project.modules.issue.enums.IssueStatus;
import com.qlda.manage_project.modules.issue.enums.IssueType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class IssueSpecification {

    public static Specification<Issue> filterBacklog(
            Long projectId,
            IssueType issueType,
            Long assigneeId,
            IssueStatus issueStatus,
            String searchKeyword) {

        return (root, query, cb) -> {
            List<Predicate> predicates = buildCommonPredicates(
                    root, cb, projectId, issueType, assigneeId, issueStatus, searchKeyword
            );
            predicates.add(cb.isNull(root.get("sprint")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Issue> filterSprintIssues(
            Long projectId,
            List<Long> sprintIds,
            IssueType issueType,
            Long assigneeId,
            IssueStatus issueStatus,
            String searchKeyword) {

        return (root, query, cb) -> {
            List<Predicate> predicates = buildCommonPredicates(
                    root, cb, projectId, issueType, assigneeId, issueStatus, searchKeyword
            );
            if (sprintIds != null && !sprintIds.isEmpty()) {
                predicates.add(root.get("sprint").get("id").in(sprintIds));
            } else {
                predicates.add(cb.disjunction());
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static List<Predicate> buildCommonPredicates(
            Root<Issue> root,
            CriteriaBuilder cb,
            Long projectId,
            IssueType issueType,
            Long assigneeId,
            IssueStatus issueStatus,
            String searchKeyword) {

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("projectId"), projectId));
        predicates.add(cb.isFalse(root.get("isDeleted")));

        if (issueType != null) {
            predicates.add(cb.equal(root.get("issueType"), issueType));
        }

        if (assigneeId != null) {
            predicates.add(cb.equal(root.get("assigneeId"), assigneeId));
        }

        if (issueStatus != null) {
            predicates.add(cb.equal(root.get("status"), issueStatus));
        }

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            String pattern = "%" + searchKeyword.trim().toLowerCase() + "%";
            Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
            Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
            predicates.add(cb.or(titleLike, descLike));
        }

        return predicates;
    }
}
