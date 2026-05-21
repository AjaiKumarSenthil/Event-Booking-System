package com.bookmyshow.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

public final class AuthContext {

    private AuthContext() {
    }

    public static UUID getUserId() {
        return getPrincipal().getUserId();
    }

    public static String getEmail() {
        return getPrincipal().getEmail();
    }

    public static List<String> getRoles() {
        return getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    public static boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof AuthPrincipal;
    }

    private static AuthPrincipal getPrincipal() {
        return (AuthPrincipal) getAuthentication().getPrincipal();
    }

    private static Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthPrincipal)) {
            throw new IllegalStateException("No authenticated user in SecurityContext");
        }
        return auth;
    }
}
