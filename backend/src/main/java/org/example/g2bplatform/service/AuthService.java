package org.example.g2bplatform.service;

import org.example.g2bplatform.DTO.AuthDto;
import org.example.g2bplatform.entity.PasswordResetToken;
import org.example.g2bplatform.entity.User;
import org.example.g2bplatform.repository.PasswordResetTokenRepository;
import org.example.g2bplatform.repository.UserRepository;
import org.example.g2bplatform.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class AuthService {

    private static final String RECOVERY_MESSAGE = "If the email exists, we sent instructions.";
    private static final int TOKEN_EXPIRY_HOURS = 24;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.env:dev}")
    private String appEnv;

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository tokenRepository,
                       JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void signup(AuthDto.SignupRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new AuthException("USERNAME_CONFLICT", "이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AuthException("EMAIL_CONFLICT", "이미 사용 중인 이메일입니다.");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        boolean isSuperAdmin = "super_admin".equalsIgnoreCase(req.getUsername());
        user.setRole(isSuperAdmin ? "ROLE_SUPER_ADMIN" : "ROLE_USER");
        user.setApproved(isSuperAdmin); // 일반 가입은 미승인, super_admin만 즉시 승인
        userRepository.save(user);
    }

    public AuthDto.LoginResponse login(AuthDto.LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new AuthException("UNAUTHORIZED", "아이디 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new AuthException("UNAUTHORIZED", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (user.getApproved() == null || !user.getApproved()) {
            throw new AuthException("NOT_APPROVED", "승인 대기 중입니다. 관리자 승인 후 이용 가능합니다.");
        }
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        AuthDto.UserInfo userInfo = new AuthDto.UserInfo(user.getUsername(), user.getRole());
        return new AuthDto.LoginResponse(token, jwtUtil.getExpiresInSeconds(), userInfo);
    }

    public AuthDto.MeResponse me(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("UNAUTHORIZED", "인증되지 않았습니다."));
        String masked = maskEmail(user.getEmail());
        return new AuthDto.MeResponse(user.getUsername(), masked, user.getRole());
    }

    @Transactional
    public AuthDto.RecoveryRequestResponse recoveryRequest(AuthDto.RecoveryRequest req) {
        Optional<User> optUser = userRepository.findByEmail(req.getEmail());
        if (optUser.isPresent()) {
            User user = optUser.get();
            String plainToken = generateSecureToken();
            String tokenHash = hashToken(plainToken);

            PasswordResetToken prt = new PasswordResetToken();
            prt.setUser(user);
            prt.setTokenHash(tokenHash);
            prt.setExpiresAt(Instant.now().plusSeconds(TOKEN_EXPIRY_HOURS * 3600L));
            tokenRepository.save(prt);

            if ("dev".equalsIgnoreCase(appEnv)) {
                return new AuthDto.RecoveryRequestResponse(RECOVERY_MESSAGE, plainToken);
            }
        }
        return new AuthDto.RecoveryRequestResponse(RECOVERY_MESSAGE);
    }

    @Transactional
    public void recoveryReset(AuthDto.RecoveryResetRequest req) {
        String tokenHash = hashToken(req.getToken());
        PasswordResetToken prt = tokenRepository
                .findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(tokenHash, Instant.now())
                .orElseThrow(() -> new AuthException("TOKEN_INVALID", "토큰이 유효하지 않거나 만료되었습니다."));

        User user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        prt.setUsedAt(Instant.now());
        tokenRepository.save(prt);
    }

    @Transactional
    public void changePassword(String username, AuthDto.PasswordChangeRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("UNAUTHORIZED", "인증되지 않았습니다."));
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthException("BAD_REQUEST", "현재 비밀번호가 올바르지 않습니다.");
        }
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    private static String maskEmail(String email) {
        if (email == null || email.length() < 5) return "***";
        int at = email.indexOf('@');
        if (at <= 0) return "***";
        String local = email.substring(0, Math.min(2, at));
        return local + "***" + email.substring(at);
    }

    private static String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static class AuthException extends RuntimeException {
        private final String code;

        public AuthException(String code, String message) {
            super(message);
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
