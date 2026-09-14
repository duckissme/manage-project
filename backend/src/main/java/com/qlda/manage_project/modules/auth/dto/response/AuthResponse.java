package com.qlda.manage_project.modules.auth.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private Long userId;
    private String fullname;
    private String email;
    private String avatar;
    private String role;
}
