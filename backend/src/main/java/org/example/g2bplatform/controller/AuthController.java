package org.example.g2bplatform.controller;

import jakarta.validation.Valid;
import org.example.g2bplatform.DTO.AuthDto;
import org.example.g2bplatform.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody AuthDto.SignupRequest req) {
        authService.signup(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.LoginResponse> login(@Valid @RequestBody AuthDto.LoginRequest req) {
        AuthDto.LoginResponse res = authService.login(req);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthDto.MeResponse> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthDto.MeResponse res = authService.me(auth.getName());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/recovery/request")
    public ResponseEntity<AuthDto.RecoveryRequestResponse> recoveryRequest(
            @Valid @RequestBody AuthDto.RecoveryRequest req) {
        AuthDto.RecoveryRequestResponse res = authService.recoveryRequest(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/recovery/reset")
    public ResponseEntity<Void> recoveryReset(@Valid @RequestBody AuthDto.RecoveryResetRequest req) {
        authService.recoveryReset(req);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(AuthService.AuthException.class)
    public ResponseEntity<AuthDto.ApiError> handleAuthException(AuthService.AuthException e) {
        HttpStatus status = switch (e.getCode()) {
            case "USERNAME_CONFLICT", "EMAIL_CONFLICT" -> HttpStatus.CONFLICT;
            case "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "TOKEN_INVALID", "BAD_REQUEST" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(new AuthDto.ApiError(e.getCode(), e.getMessage()));
    }
}
