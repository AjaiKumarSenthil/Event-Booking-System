package com.bookmyshow.auth.service;

import com.bookmyshow.auth.enums.RoleName;
import com.bookmyshow.auth.model.AuthResponse;
import com.bookmyshow.auth.model.LoginRequest;
import com.bookmyshow.auth.model.RegisterRequest;
import com.bookmyshow.auth.model.UserContactResponse;
import com.bookmyshow.auth.model.UserProfile;

import java.util.UUID;

public interface IAuthService {

    UserProfile register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String refreshToken);

    UserProfile getCurrentUser(UUID userId);

    UserProfile assignRole(UUID userId, RoleName roleName);

    UserContactResponse getUserContact(UUID userId);
}
