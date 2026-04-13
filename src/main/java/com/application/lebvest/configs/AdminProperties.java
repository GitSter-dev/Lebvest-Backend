package com.application.lebvest.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "lebvest.admin")
public record AdminProperties(
        String email,
        String name,
        String seedPassword,
        Jwt jwt
) {

    public record Jwt(String secret, Duration expiration) {
    }
}
