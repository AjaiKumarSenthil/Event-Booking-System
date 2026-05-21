package com.bookmyshow.auth.service.impl;

import com.bookmyshow.auth.entity.RefreshToken;
import com.bookmyshow.auth.entity.Role;
import com.bookmyshow.auth.entity.User;
import com.bookmyshow.auth.enums.RoleName;
import com.bookmyshow.auth.exception.ConflictException;
import com.bookmyshow.auth.exception.EmailAlreadyExistsException;
import com.bookmyshow.auth.exception.InvalidCredentialsException;
import com.bookmyshow.auth.exception.InvalidTokenException;
import com.bookmyshow.auth.exception.ResourceNotFoundException;
import com.bookmyshow.auth.model.AuthResponse;
import com.bookmyshow.auth.model.LoginRequest;
import com.bookmyshow.auth.model.RegisterRequest;
import com.bookmyshow.auth.model.UserContactResponse;
import com.bookmyshow.auth.model.UserProfile;
import com.bookmyshow.auth.repository.RoleRepository;
import com.bookmyshow.auth.repository.UserRepository;
import com.bookmyshow.auth.config.JwtProperties;
import com.bookmyshow.auth.security.JwtService;
import com.bookmyshow.auth.service.IAuthService;
import com.bookmyshow.jwt.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public UserProfile register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setEnabled(true);
        user.setRoles(Set.of(userRole));

        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        user = userRepository.save(user);
        return toUserProfile(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = jwtService.createRefreshToken(user);

        return new AuthResponse()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn((int) jwtProperties.accessTokenExpiry().toSeconds());
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = jwtService.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();
        String accessToken = jwtService.generateAccessToken(user);

        return new AuthResponse()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn((int) jwtProperties.accessTokenExpiry().toSeconds());
    }

    @Override
    @Transactional
    public void logout(String refreshTokenValue) {
        RefreshToken refreshToken = jwtService.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        jwtService.revokeRefreshToken(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfile getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        return toUserProfile(user);
    }

    @Override
    @Transactional
    public UserProfile assignRole(UUID userId, RoleName roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        boolean alreadyHasRole = user.getRoles().stream()
                .anyMatch(r -> r.getName() == roleName);
        if (alreadyHasRole) {
            throw new ConflictException("User " + userId + " already has role " + roleName);
        }

        user.getRoles().add(role);
        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        return toUserProfile(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserContactResponse getUserContact(UUID userId) {
        UUID callerId = AuthContext.getUserId();
        if (!callerId.equals(userId) && !AuthContext.hasRole(ROLE_ADMIN)) {
            throw new AccessDeniedException(
                    "Caller is not authorized to read contact for user " + userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        return new UserContactResponse()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName());
    }

    private UserProfile toUserProfile(User user) {
        return new UserProfile()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .map(Enum::name)
                        .toList())
                .createdAt(user.getCreatedAt().atOffset(ZoneOffset.UTC));
    }
}
