package com.qlda.manage_project.modules.auth.service;

import com.qlda.manage_project.modules.auth.dto.request.RegisterRequest;
import com.qlda.manage_project.modules.user.entity.UserEntity;

public interface AuthService {
    void register(RegisterRequest request);
    UserEntity getCurrentUser();
}
