package org.example.g2bplatform.controller;

import org.example.g2bplatform.DTO.AuthDto;
import org.example.g2bplatform.service.AdminService;
import org.example.g2bplatform.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminService.UserSummary>> listUsers(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean adminOrSuper = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
        if (!adminOrSuper) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(adminService.listUsers());
    }

    @PatchMapping("/users/{id}/approve")
    public ResponseEntity<Void> approveUser(Authentication auth, @PathVariable Long id) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        adminService.approveUser(id, auth.getName());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<Void> updateUserRole(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String role = body != null ? body.get("role") : null;
        if (role == null || role.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        adminService.updateUserRole(id, role.trim(), auth.getName());
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(AuthService.AuthException.class)
    public ResponseEntity<AuthDto.ApiError> handleAuthException(AuthService.AuthException e) {
        HttpStatus status = switch (e.getCode()) {
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(new AuthDto.ApiError(e.getCode(), e.getMessage()));
    }
}
