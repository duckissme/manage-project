package com.qlda.manage_project.modules.project.dto.response;

import com.qlda.manage_project.modules.project.enums.ProjectStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectRes {
    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate deadline;
}
