package org.example.g2bplatform.controller;

import jakarta.validation.Valid;
import org.example.g2bplatform.DTO.AuthDto;
import org.example.g2bplatform.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(Authentication auth,
                                               @Valid @RequestBody AuthDto.PasswordChangeRequest req) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        authService.changePassword(auth.getName(), req);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(AuthService.AuthException.class)
    public ResponseEntity<AuthDto.ApiError> handleAuthException(AuthService.AuthException e) {
        HttpStatus status = switch (e.getCode()) {
            case "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "BAD_REQUEST" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(new AuthDto.ApiError(e.getCode(), e.getMessage()));
    }
}
