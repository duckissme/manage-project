package com.qlda.manage_project.modules.sprint.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qlda.manage_project.modules.backlog.dto.response.IssueBacklogResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SprintResponse {
    private Long id;
    private String name;
    private Long projectId;
    private String goal;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private LocalDateTime createdAt;

    private Integer totalTasks;
    private Integer totalStoryPoints;
    private Integer toDoPoints;
    private Integer inProgressPoints;
    private Integer donePoints;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<IssueBacklogResponse> issues;
}
