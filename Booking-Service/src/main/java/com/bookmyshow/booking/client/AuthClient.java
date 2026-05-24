package com.bookmyshow.booking.client;

import com.bookmyshow.booking.client.dto.UserContact;
import com.bookmyshow.booking.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Slf4j
@Component
public class AuthClient {

    private final RestClient restClient;

    public AuthClient(@Value("${auth.base-url}") String baseUrl,
                      BearerTokenForwardingInterceptor authForwardingInterceptor) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(authForwardingInterceptor)
                .build();
    }

    public UserContact getUserContact(UUID userId) {
        return restClient.get()
                .uri("/internal/users/{userId}", userId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    if (res.getStatusCode().value() == 404) {
                        log.warn("Auth getUserContact 404: userId={}", userId);
                        throw new ResourceNotFoundException("User not found in auth service: " + userId);
                    }
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.error("Auth getUserContact 5xx: userId={} status={}", userId, res.getStatusCode());
                })
                .body(UserContact.class);
    }
}
