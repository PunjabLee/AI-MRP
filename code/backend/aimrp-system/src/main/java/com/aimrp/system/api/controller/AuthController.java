package com.aimrp.system.api.controller;

import com.aimrp.common.result.ApiResponse;
import com.aimrp.system.application.dto.LoginRequest;
import com.aimrp.system.application.dto.LoginResponse;
import com.aimrp.system.infrastructure.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证 Controller
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    
    /**
     * 登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String username = authentication.getName();
        Long userId = 1L; // TODO: 从认证信息中获取用户 ID
        
        String token = jwtUtils.generateToken(userId, username);
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setExpiresIn(86400000L);
        
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(userId);
        userInfo.setUsername(username);
        response.setUser(userInfo);
        
        return ApiResponse.ok(response);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<LoginResponse.UserInfo> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setUsername(username);
        
        return ApiResponse.ok(userInfo);
    }
    
    /**
     * 登出
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        SecurityContextHolder.clearContext();
        return ApiResponse.ok();
    }
}
