package com.qlda.manage_project.modules.auth.service.impl;

import com.qlda.manage_project.infrastructure.security.CustomUserDetails;
import com.qlda.manage_project.modules.auth.dto.request.RegisterRequest;
import com.qlda.manage_project.modules.auth.service.AuthService;
import com.qlda.manage_project.modules.user.entity.UserEntity;
import com.qlda.manage_project.modules.user.repository.RoleRepository;
import com.qlda.manage_project.modules.user.repository.UserRepository;
import com.qlda.manage_project.common.exception.BadRequestException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @NonFinal
    @Value("${jwt.secret}")
    String jwtSecret;

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    @Transactional
    @Override
    public void register(RegisterRequest request){
        boolean existEmail = userRepository.existsByEmail(request.getEmail());
        if (existEmail) {
            throw new BadRequestException("Tài khoản đã tồn tại");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setAvatar("https://res.cloudinary.com/dcjraarbb/image/upload/v1775368268/vj9ueejpcaablfaamdk7.webp");
        userEntity.setFullName(request.getFullname());
        userEntity.setEmail(request.getEmail());
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        userEntity.setRole(roleRepository.findByName("STAFF").get());
        userRepository.save(userEntity);
    }

    @Override
    public UserEntity getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || authentication.getPrincipal() == null){
            throw new BadRequestException("Người dùng chưa đăng nhập");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }
}
