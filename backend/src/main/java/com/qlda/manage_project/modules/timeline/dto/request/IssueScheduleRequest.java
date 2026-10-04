package com.qlda.manage_project.modules.timeline.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IssueScheduleRequest {
    private LocalDateTime startDate;
    private LocalDateTime dueDate;

    @NotNull(message = "Version không được để trống")
    private Integer version;
}
