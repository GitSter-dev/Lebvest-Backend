package com.application.lebvest.services;

import com.application.lebvest.configs.AdminProperties;
import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.InvestorAuthTokenPayloadDto;
import com.application.lebvest.models.dtos.InvestorLoginRequestDto;
import com.application.lebvest.models.dtos.InvestorSetPasswordRequestDto;
import com.application.lebvest.models.dtos.InvestorSetPasswordResponseDto;
import com.application.lebvest.models.entities.InvestorAccount;
import com.application.lebvest.models.entities.InvestorApplication;
import com.application.lebvest.models.entities.SetPasswordToken;
import com.application.lebvest.models.enums.InvestorApplicationStatus;
import com.application.lebvest.models.exceptions.InvalidStateException;
import com.application.lebvest.models.exceptions.ResourceConflictException;
import com.application.lebvest.models.exceptions.UnauthorizedException;
import com.application.lebvest.repositories.InvestorAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestorAuthService {

    private static final String TOKEN_TYPE_BEARER = "Bearer";
    private static final String INVESTOR_ROLE = "INVESTOR";

    private final InvestorAccountRepository investorAccountRepository;
    private final SetPasswordTokenService setPasswordTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final AdminProperties adminProperties;
    private final Clock clock;

    @Transactional
    public ApiResponseDto<InvestorSetPasswordResponseDto> setPassword(InvestorSetPasswordRequestDto req) {
        SetPasswordToken setPasswordToken = setPasswordTokenService.getValidatedToken(req.token());
        InvestorApplication application = setPasswordToken.getApplication();

        if (application.getApplicationStatus() != InvestorApplicationStatus.ACCEPTED) {
            throw new InvalidStateException(
                    "Investor application with id = "
                            + application.getId()
                            + " is not approved yet"
            );
        }

        if (investorAccountRepository.existsByEmail(application.getEmail())
                || investorAccountRepository.existsByApplication_Id(application.getId())) {
            throw new ResourceConflictException(InvestorAccount.class, "email", application.getEmail());
        }

        InvestorAccount investorAccount = InvestorAccount.builder()
                .email(application.getEmail())
                .passwordHash(passwordEncoder.encode(req.password()))
                .enabled(true)
                .application(application)
                .build();
        investorAccount = investorAccountRepository.save(investorAccount);

        setPasswordTokenService.invalidateToken(req.token());
        log.info("Investor account created applicationId={}, email={}", application.getId(), application.getEmail());

        return ApiResponseDto.ok(
                HttpStatus.OK.value(),
                InvestorSetPasswordResponseDto.builder()
                        .accountId(investorAccount.getId())
                        .email(investorAccount.getEmail())
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<InvestorAuthTokenPayloadDto> login(InvestorLoginRequestDto request) {
        InvestorAccount account = investorAccountRepository.findByEmail(request.email().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!account.isEnabled()) {
            throw new UnauthorizedException("Invalid email or password");
        }
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        Instant issuedAt = clock.instant();
        Duration ttl = resolveAccessTokenTtl();
        Instant expiresAt = issuedAt.plus(ttl);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("lebvest-investor")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(account.getEmail())
                .claim("email", account.getEmail())
                .claim("role", INVESTOR_ROLE)
                .build();

        JwsHeader header = JwsHeader.with(() -> "HS256").build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        InvestorAuthTokenPayloadDto payload = InvestorAuthTokenPayloadDto.builder()
                .accessToken(accessToken)
                .tokenType(TOKEN_TYPE_BEARER)
                .expiresAt(expiresAt)
                .email(account.getEmail())
                .role(INVESTOR_ROLE)
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
