package org.example.g2bplatform.service;

import org.example.g2bplatform.entity.User;
import org.example.g2bplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 전체 유저 목록 (ADMIN 이상만 호출 가능).
     * 비밀번호 등 민감 정보 제외.
     */
    public List<UserSummary> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /**
     * role 변경. SUPER_ADMIN만 호출 가능.
     */
    @Transactional
    public void updateUserRole(Long userId, String role, String currentUsername) {
        User current = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AuthService.AuthException("UNAUTHORIZED", "인증되지 않았습니다."));
        if (!ROLE_SUPER_ADMIN.equals(current.getRole())) {
            throw new AuthService.AuthException("FORBIDDEN", "권한이 없습니다. 슈퍼관리자만 역할을 변경할 수 있습니다.");
        }
        if (!ROLE_USER.equals(role) && !ROLE_ADMIN.equals(role) && !ROLE_SUPER_ADMIN.equals(role)) {
            throw new AuthService.AuthException("BAD_REQUEST", "유효하지 않은 역할입니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthService.AuthException("NOT_FOUND", "사용자를 찾을 수 없습니다."));
        user.setRole(role);
        userRepository.save(user);
    }

    private UserSummary toSummary(User u) {
        UserSummary s = new UserSummary();
        s.setId(u.getId());
        s.setUsername(u.getUsername());
        s.setEmail(maskEmail(u.getEmail()));
        s.setRole(u.getRole());
        s.setCreatedAt(u.getCreatedAt());
        return s;
    }

    private static String maskEmail(String email) {
        if (email == null || email.length() < 5) return "***";
        int at = email.indexOf('@');
        if (at <= 0) return "***";
        String local = email.substring(0, Math.min(2, at));
        return local + "***" + email.substring(at);
    }

    public static class UserSummary {
        private Long id;
        private String username;
        private String email;
        private String role;
        private java.time.Instant createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public java.time.Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.Instant createdAt) { this.createdAt = createdAt; }
    }
}
