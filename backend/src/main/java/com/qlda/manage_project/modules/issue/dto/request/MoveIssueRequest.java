package com.qlda.manage_project.modules.issue.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MoveIssueRequest {
    private Long targetSprintId;

    @NotNull(message = "Version không được để trống")
    private Integer version;
}
