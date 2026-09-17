package com.qlda.manage_project.modules.issue.dto.request;

import com.qlda.manage_project.modules.issue.enums.IssueLinkType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IssueLinkCreateRequest {

    @NotNull(message = "Target issue ID không được để trống")
    private Long targetIssueId;

    @NotNull(message = "Link type không được để trống")
    private IssueLinkType linkType;
}
