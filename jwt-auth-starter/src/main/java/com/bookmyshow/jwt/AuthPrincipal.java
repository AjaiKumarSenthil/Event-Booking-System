package com.bookmyshow.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AuthPrincipal {

    private final UUID userId;
    private final String email;
}
