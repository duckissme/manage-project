package com.qlda.manage_project.modules.sprint.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SprintCreateRequest {
    @Size(max = 255, message = "Tên Sprint không được vượt quá 255 ký tự")
    private String name;

    private String goal;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
