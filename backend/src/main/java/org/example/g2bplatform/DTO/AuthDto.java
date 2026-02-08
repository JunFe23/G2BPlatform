package org.example.g2bplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDto {

    public static class SignupRequest {
        @NotBlank(message = "아이디는 필수입니다")
        @Size(min = 1, max = 100)
        private String username;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        private String password;

        @NotBlank(message = "이메일은 필수입니다")
        @Email
        @Size(max = 255)
        private String email;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginResponse {
        private String accessToken;
        private String tokenType = "Bearer";
        private long expiresIn;
        private UserInfo user;

        public LoginResponse(String accessToken, long expiresIn, UserInfo user) {
            this.accessToken = accessToken;
            this.expiresIn = expiresIn;
            this.user = user;
        }

        public String getAccessToken() { return accessToken; }
        public String getTokenType() { return tokenType; }
        public long getExpiresIn() { return expiresIn; }
        public UserInfo getUser() { return user; }
    }

    public static class UserInfo {
        private String username;
        private String role;

        public UserInfo(String username, String role) {
            this.username = username;
            this.role = role;
        }

        public String getUsername() { return username; }
        public String getRole() { return role; }
    }

    public static class MeResponse {
        private String username;
        private String emailMasked;
        private String role;

        public MeResponse(String username, String emailMasked, String role) {
            this.username = username;
            this.emailMasked = emailMasked;
            this.role = role;
        }

        public String getUsername() { return username; }
        public String getEmailMasked() { return emailMasked; }
        public String getRole() { return role; }
    }

    public static class RecoveryRequest {
        @NotBlank(message = "이메일은 필수입니다")
        @Email
        private String email;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class RecoveryRequestResponse {
        private String message;
        private String devResetToken; // DEV only

        public RecoveryRequestResponse(String message) { this.message = message; }
        public RecoveryRequestResponse(String message, String devResetToken) {
            this.message = message;
            this.devResetToken = devResetToken;
        }

        public String getMessage() { return message; }
        public String getDevResetToken() { return devResetToken; }
    }

    public static class RecoveryResetRequest {
        @NotBlank
        private String token;
        @NotBlank
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        private String newPassword;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class PasswordChangeRequest {
        @NotBlank
        private String currentPassword;
        @NotBlank
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        private String newPassword;

        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class ApiError {
        private String code;
        private String message;
        private String detail;

        public ApiError(String code, String message) { this.code = code; this.message = message; }
        public ApiError(String code, String message, String detail) {
            this.code = code; this.message = message; this.detail = detail;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
        public String getDetail() { return detail; }
    }
}
