package com.bookmyshow.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.Resource;

import java.time.Duration;


@ConfigurationProperties("jwt")
public record JwtProperties(
        Resource privateKey,
        Resource publicKey,
        @DefaultValue("30m") Duration accessTokenExpiry,
        @DefaultValue("7d") Duration refreshTokenExpiry
) {
}
