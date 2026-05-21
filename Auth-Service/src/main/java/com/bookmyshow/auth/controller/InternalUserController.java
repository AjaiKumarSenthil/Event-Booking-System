package com.bookmyshow.auth.controller;

import com.bookmyshow.auth.api.InternalApi;
import com.bookmyshow.auth.model.UserContactResponse;
import com.bookmyshow.auth.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InternalUserController implements InternalApi {

    private final IAuthService authService;

    @Override
    public ResponseEntity<UserContactResponse> getUserContact(UUID userId) {
        return ResponseEntity.ok(authService.getUserContact(userId));
    }
}
