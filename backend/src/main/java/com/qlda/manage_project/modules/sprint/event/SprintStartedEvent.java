package com.qlda.manage_project.modules.sprint.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SprintStartedEvent {
    private Long projectId;
    private Long sprintId;
    private String sprintName;
    private Long actorId;
}
