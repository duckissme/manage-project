package com.qlda.manage_project.modules.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreateReq {
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate deadline;
}
