package com.qlda.manage_project.modules.project.dto.response;

import com.qlda.manage_project.modules.project.enums.ProjectRole;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberRes {
    private Long id;
    private Long projectId;
    private Long userId;
    private String username;
    private String email;
    private ProjectRole role;
    private LocalDateTime joinedAt;
}
