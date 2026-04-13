package com.application.lebvest.services;

import com.application.lebvest.configs.AdminProperties;
import com.application.lebvest.models.dtos.AdminAuthTokenPayloadDto;
import com.application.lebvest.models.dtos.AdminLoginRequestDto;
import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.entities.AdminUser;
import com.application.lebvest.models.exceptions.UnauthorizedException;
import com.application.lebvest.repositories.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private final AdminUserRepository adminUserRepository;
    private final JwtEncoder jwtEncoder;
    private final AdminProperties adminProperties;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Transactional(readOnly = true)
    public ApiResponseDto<AdminAuthTokenPayloadDto> login(AdminLoginRequestDto request) {
        AdminUser user = adminUserRepository.findByEmail(request.email().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!user.isEnabled()) {
            throw new UnauthorizedException("Invalid email or password");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        Instant issuedAt = clock.instant();
        Duration ttl = resolveAccessTokenTtl();
        Instant expiresAt = issuedAt.plus(ttl);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("lebvest-admin")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .build();

        JwsHeader header = JwsHeader.with(() -> "HS256").build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        AdminAuthTokenPayloadDto payload = AdminAuthTokenPayloadDto.builder()
                .accessToken(accessToken)
                .tokenType(TOKEN_TYPE_BEARER)
                .expiresAt(expiresAt)
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return ApiResponseDto.ok(HttpStatus.OK.value(), payload);
    }

    private Duration resolveAccessTokenTtl() {
        AdminProperties.Jwt jwt = adminProperties.jwt();
        if (jwt == null || jwt.expiration() == null) {
            return Duration.ofMinutes(15);
        }
        return jwt.expiration();
    }
}
