package com.bookmyshow.booking.client;

import com.bookmyshow.booking.client.dto.UserContact;
import com.bookmyshow.booking.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

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
                        throw new ResourceNotFoundException("User not found in auth service: " + userId);
                    }
                })
                .body(UserContact.class);
    }
}
