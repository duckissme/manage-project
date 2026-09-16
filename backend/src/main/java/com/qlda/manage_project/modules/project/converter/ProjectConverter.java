package com.qlda.manage_project.modules.project.converter;

import com.qlda.manage_project.modules.project.dto.response.ProjectRes;
import com.qlda.manage_project.modules.project.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectConverter {
    public ProjectRes mapToResponse(Project project) {
        if (project == null) {
            return null;
        }

        return ProjectRes.builder()
                .id(project.getId())
                .projectKey(project.getProjectKey())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .deadline(project.getDeadline())
                .build();
    }
}
