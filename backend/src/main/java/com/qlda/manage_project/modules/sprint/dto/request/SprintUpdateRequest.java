package com.qlda.manage_project.modules.sprint.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SprintUpdateRequest {
    @Size(max = 255, message = "Tên Sprint không được vượt quá 255 ký tự")
    private String name;

    private String goal;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
