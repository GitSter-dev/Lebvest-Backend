package com.application.lebvest.services;

import com.application.lebvest.models.entities.InvestorApplication;
import com.application.lebvest.models.entities.SetPasswordToken;
import com.application.lebvest.models.exceptions.BadRequestException;
import com.application.lebvest.repositories.SetPasswordTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class SetPasswordTokenService {

    private static final long TOKEN_TTL_SECONDS = 86400;
    private static final String HMAC_ALGO = "HmacSHA256";

    private final Clock clock;
    private final Supplier<UUID> uuidSupplier;
    private final SetPasswordTokenRepository setPasswordTokenRepository;

    @Value("${lebvest.frontend-url}")
    private String frontendUrl;

    @Value("${lebvest.set-password-token-secret}")
    private String tokenSecret;

    @Transactional
    public String createSetPasswordUrl(InvestorApplication application) {
        setPasswordTokenRepository.deleteByApplicationId(application.getId());

        String token = createToken(application.getId());
        SetPasswordToken setPasswordToken = SetPasswordToken.builder()
                .token(token)
                .application(application)
                .build();
        setPasswordTokenRepository.save(setPasswordToken);

        return frontendUrl + "/investor-applications/" + application.getId() + "?token=" + token;
    }

    public SetPasswordToken getValidatedToken(String token) {
        DecodedToken decodedToken = decodeAndVerify(token);

        SetPasswordToken persistedToken = setPasswordTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Set password token is invalid or has already been used"));

        if (!persistedToken.getApplication().getId().equals(decodedToken.applicationId())) {
            throw new BadRequestException("Set password token is invalid");
        }

        if (decodedToken.expiry() < Instant.now(clock).getEpochSecond()) {
            setPasswordTokenRepository.delete(persistedToken);
            throw new BadRequestException("Set password token has expired");
        }

        return persistedToken;
    }

    @Transactional
    public void invalidateToken(String token) {
        setPasswordTokenRepository.findByToken(token)
                .ifPresent(setPasswordTokenRepository::delete);
    }

    @Transactional
    public void invalidateTokensForApplication(Long applicationId) {
        setPasswordTokenRepository.deleteByApplicationId(applicationId);
    }

    private String createToken(Long applicationId) {
        long expiry = Instant.now(clock).plusSeconds(TOKEN_TTL_SECONDS).getEpochSecond();
        String nonce = uuidSupplier.get().toString();

        String payload = applicationId + "." + expiry + "." + nonce;
        String signature = hmacSha256Base64Url(payload, tokenSecret);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString((payload + "." + signature).getBytes(StandardCharsets.UTF_8));
    }

    private DecodedToken decodeAndVerify(String token) {
        try {
            String decodedToken = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decodedToken.split("\\.");
            if (parts.length != 4) {
                throw new BadRequestException("Set password token is invalid");
            }

            Long applicationId = Long.parseLong(parts[0]);
            long expiry = Long.parseLong(parts[1]);
            String nonce = parts[2];
            String signature = parts[3];

            String payload = applicationId + "." + expiry + "." + nonce;
            String expectedSignature = hmacSha256Base64Url(payload, tokenSecret);
            if (!expectedSignature.equals(signature)) {
                throw new BadRequestException("Set password token is invalid");
            }

            return new DecodedToken(applicationId, expiry);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Set password token is invalid");
        }
    }

    private String hmacSha256Base64Url(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
            mac.init(keySpec);
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException("Could not sign token", e);
        }
    }

    private record DecodedToken(Long applicationId, long expiry) {
    }
}
