package com.qlda.manage_project.modules.issue.service.impl;

import com.qlda.manage_project.common.exception.BadRequestException;
import com.qlda.manage_project.common.exception.ForbiddenException;
import com.qlda.manage_project.common.exception.NotFoundException;
import com.qlda.manage_project.modules.issue.converter.IssueConverter;
import com.qlda.manage_project.modules.issue.dto.request.IssueLinkCreateRequest;
import com.qlda.manage_project.modules.issue.dto.response.IssueLinkResponse;
import com.qlda.manage_project.modules.issue.entity.Issue;
import com.qlda.manage_project.modules.issue.entity.IssueLink;
import com.qlda.manage_project.modules.issue.enums.IssueLinkType;
import com.qlda.manage_project.modules.issue.event.IssueUpdatedEvent;
import com.qlda.manage_project.modules.issue.repository.IssueLinkRepository;
import com.qlda.manage_project.modules.issue.repository.IssueRepository;
import com.qlda.manage_project.modules.issue.service.IssueLinkService;
import com.qlda.manage_project.modules.project.entity.ProjectMember;
import com.qlda.manage_project.modules.project.enums.ProjectRole;
import com.qlda.manage_project.modules.project.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueLinkServiceImpl implements IssueLinkService {

    private final IssueLinkRepository issueLinkRepository;
    private final IssueRepository issueRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final IssueConverter issueConverter;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public IssueLinkResponse createLink(Long sourceIssueId, IssueLinkCreateRequest request, Long userId) {
        Issue sourceIssue = issueRepository.findById(sourceIssueId)
                .filter(i -> !i.getIsDeleted())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy Issue nguồn hoặc đã bị xóa"));

        Issue targetIssue = issueRepository.findById(request.getTargetIssueId())
                .filter(i -> !i.getIsDeleted())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy Issue đích hoặc đã bị xóa"));

        // Chặn tự liên kết với chính nó
        if (sourceIssue.getId().equals(targetIssue.getId())) {
            throw new BadRequestException("Không thể liên kết một Issue với chính nó");
        }

        // Kiểm tra cùng dự án
        if (!sourceIssue.getProject().getId().equals(targetIssue.getProject().getId())) {
            throw new BadRequestException("Hai Issue phải thuộc cùng một dự án");
        }

        // Kiểm tra quyền thành viên trong dự án
        projectMemberRepository.findByProjectIdAndUserIdAndIsDeletedFalse(sourceIssue.getProject().getId(), userId)
                .orElseThrow(() -> new ForbiddenException("Bạn không phải thành viên của dự án"));

        // Chặn liên kết trùng lặp
        if (issueLinkRepository.existsBySourceIssueIdAndTargetIssueIdAndLinkType(
                sourceIssue.getId(), targetIssue.getId(), request.getLinkType())) {
            throw new BadRequestException("Liên kết này đã tồn tại giữa 2 Issue");
        }

        // Với quan hệ đối xứng RELATES_TO
        if (request.getLinkType() == IssueLinkType.RELATES_TO &&
                issueLinkRepository.existsBySourceIssueIdAndTargetIssueIdAndLinkType(
                        targetIssue.getId(), sourceIssue.getId(), IssueLinkType.RELATES_TO)) {
            throw new BadRequestException("Hai Issue này đã có quan hệ liên quan (relates to)");
        }

        // Với quan hệ BLOCKS: Chặn phụ thuộc vòng lặp (Circular Dependency)
        if (request.getLinkType() == IssueLinkType.BLOCKS &&
                issueLinkRepository.existsBySourceIssueIdAndTargetIssueIdAndLinkType(
                        targetIssue.getId(), sourceIssue.getId(), IssueLinkType.BLOCKS)) {
            throw new BadRequestException("Không thể tạo liên kết: Issue đích đang chặn Issue nguồn (phụ thuộc vòng tròn)");
        }

        IssueLink link = IssueLink.builder()
                .sourceIssue(sourceIssue)
                .targetIssue(targetIssue)
                .linkType(request.getLinkType())
                .createdBy(userId)
                .build();

        IssueLink savedLink = issueLinkRepository.save(link);

        // Ghi Audit Log cho Source Issue
        String logDetail = "Liên kết với " + targetIssue.getIssueKey() + " (" + request.getLinkType().getOutwardDescription() + ")";
        eventPublisher.publishEvent(new IssueUpdatedEvent(
                sourceIssue.getId(),
                userId,
                List.of(new IssueUpdatedEvent.Change("Link", null, logDetail))
        ));

        return IssueLinkResponse.builder()
                .id(savedLink.getId())
                .linkType(savedLink.getLinkType())
                .relationship(savedLink.getLinkType().getOutwardDescription())
                .linkedIssue(issueConverter.mapToSummaryResponse(targetIssue))
                .createdAt(savedLink.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<IssueLinkResponse> getLinksByIssue(Long issueId) {
        issueRepository.findById(issueId)
                .filter(i -> !i.getIsDeleted())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy Issue hoặc đã bị xóa"));

        List<IssueLink> links = issueLinkRepository.findAllLinksByIssueId(issueId);

        return links.stream().map(link -> {
            boolean isOutward = link.getSourceIssue().getId().equals(issueId);
            Issue linkedIssue = isOutward ? link.getTargetIssue() : link.getSourceIssue();
            String relationship = link.getLinkType().getRelationship(isOutward);

            return IssueLinkResponse.builder()
                    .id(link.getId())
                    .linkType(link.getLinkType())
                    .relationship(relationship)
                    .linkedIssue(issueConverter.mapToSummaryResponse(linkedIssue))
                    .createdAt(link.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteLink(Long linkId, Long userId) {
        IssueLink link = issueLinkRepository.findById(linkId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy liên kết với ID: " + linkId));

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserIdAndIsDeletedFalse(
                link.getSourceIssue().getProject().getId(), userId)
                .orElseThrow(() -> new ForbiddenException("Bạn không phải thành viên của dự án"));

        if (!link.getCreatedBy().equals(userId)
                && member.getProjectRole() != ProjectRole.OWNER
                && member.getProjectRole() != ProjectRole.MANAGER) {
            throw new ForbiddenException("Chỉ người tạo liên kết hoặc Quản trị viên dự án mới có quyền xóa liên kết này");
        }

        // Ghi Audit Log trước khi xóa
        String logDetail = "Gỡ liên kết với " + link.getTargetIssue().getIssueKey() + " (" + link.getLinkType().getOutwardDescription() + ")";
        eventPublisher.publishEvent(new IssueUpdatedEvent(
                link.getSourceIssue().getId(),
                userId,
                List.of(new IssueUpdatedEvent.Change("Link", logDetail, null))
        ));

        issueLinkRepository.delete(link);
    }
}
