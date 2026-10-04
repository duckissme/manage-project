package com.qlda.manage_project.modules.timeline.dto.response;

import com.qlda.manage_project.modules.sprint.enums.SprintStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimelineSprintResponse {
    private Long id;
    private String name;
    private SprintStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
