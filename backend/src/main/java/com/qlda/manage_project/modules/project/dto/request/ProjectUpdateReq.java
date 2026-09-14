package com.qlda.manage_project.modules.project.dto.request;

import com.qlda.manage_project.modules.project.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUpdateReq {
    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate deadline;
}
