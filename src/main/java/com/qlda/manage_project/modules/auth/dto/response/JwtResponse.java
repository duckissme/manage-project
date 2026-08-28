package com.qlda.manage_project.modules.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private Long userId;
    private String accessToken;
    private String email;
    private String fullname;
    private String avatar;
    private String role;
}
