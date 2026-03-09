package com.aimrp.system.application.dto;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginResponse {
    
    private String token;
    
    private String tokenType = "Bearer";
    
    private Long expiresIn;
    
    private UserInfo user;
    
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String email;
    }
}
