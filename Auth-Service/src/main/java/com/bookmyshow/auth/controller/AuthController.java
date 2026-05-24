package com.bookmyshow.auth.controller;

import com.bookmyshow.auth.api.AuthApi;
import com.bookmyshow.auth.enums.RoleName;
import com.bookmyshow.auth.model.AssignRoleRequest;
import com.bookmyshow.auth.model.AuthResponse;
import com.bookmyshow.auth.model.LoginRequest;
import com.bookmyshow.auth.model.RefreshTokenRequest;
import com.bookmyshow.auth.model.RegisterRequest;
import com.bookmyshow.auth.model.UserProfile;
import com.bookmyshow.auth.service.IAuthService;
import com.bookmyshow.jwt.AuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final IAuthService authService;

    @Override
    public ResponseEntity<UserProfile> register(RegisterRequest registerRequest) {
        log.info("Register request received for email={}", registerRequest.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(registerRequest));
    }

    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest loginRequest) {
        log.info("Login request received for email={}", loginRequest.getEmail());
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Override
    public ResponseEntity<AuthResponse> refreshToken(RefreshTokenRequest refreshTokenRequest) {
        log.info("Refresh-token request received");
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest.getRefreshToken()));
    }

    @Override
    public ResponseEntity<Void> logout(RefreshTokenRequest refreshTokenRequest) {
        log.info("Logout request received");
        authService.logout(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<UserProfile> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser(AuthContext.getUserId()));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfile> assignRole(UUID userId, AssignRoleRequest assignRoleRequest) {
        RoleName roleName = RoleName.valueOf(assignRoleRequest.getRole().name());
        log.info("AssignRole request: targetUserId={} role={}", userId, roleName);
        return ResponseEntity.ok(authService.assignRole(userId, roleName));
    }
}
