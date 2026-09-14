package com.qlda.manage_project.modules.project.dto.request;

import com.qlda.manage_project.modules.project.enums.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddMemberReq {
    private Long userIdToAdd;
    private ProjectRole role;
}
