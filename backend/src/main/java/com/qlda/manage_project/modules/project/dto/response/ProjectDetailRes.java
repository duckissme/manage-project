package com.qlda.manage_project.modules.project.dto.response;

import com.qlda.manage_project.modules.project.enums.ProjectStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectDetailRes {
    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate deadline;

    private List<ProjectMemberRes> members;
}
