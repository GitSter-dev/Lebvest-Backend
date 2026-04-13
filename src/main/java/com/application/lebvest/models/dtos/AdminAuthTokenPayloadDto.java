package com.application.lebvest.models.dtos;

import lombok.Builder;

import java.time.Instant;

@Builder
public record AdminAuthTokenPayloadDto(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        String email,
        String role
) {
}
