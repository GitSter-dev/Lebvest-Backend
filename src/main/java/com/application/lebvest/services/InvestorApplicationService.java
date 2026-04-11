package com.application.lebvest.services;

import com.application.lebvest.models.exceptions.ResourceConflictException;
import com.application.lebvest.repositories.InvestorApplicationRepository;
import com.application.lebvest.configs.AdminProperties;
import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.InvestorApplicationRequestDto;
import com.application.lebvest.models.dtos.InvestorApplicationResponseDto;
import com.application.lebvest.models.entities.InvestorApplication;
import com.application.lebvest.models.entities.SetPasswordToken;
import com.application.lebvest.models.enums.InvestorApplicationStatus;
import com.application.lebvest.models.events.AdminEvents;
import com.application.lebvest.models.events.InvestorEvents;
import com.application.lebvest.producers.InvestorEventsProducer;
import com.application.lebvest.repositories.SetPasswordTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestorApplicationService {

    private final InvestorApplicationRepository investorApplicationRepository;
    private static final String INVESTOR_DOCUMENTS_PATH_PREFIX = "investor-documents/pending";
    private final S3Service s3Service;
    private final Clock clock;
    private final Supplier<UUID> uuidSupplier;
    private final InvestorEventsProducer investorEventsProducer;
    private final HandlebarsRendererService handlebarsRendererService;
    private final StylesLoaderService stylesLoaderService;
    private final AdminProperties adminProperties;
    private final SetPasswordTokenRepository setPasswordTokenRepository;

    private static final DateTimeFormatter SUBMITTED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(ZoneOffset.UTC);

    @Value("${lebvest.frontend-url}")
    private String frontendUrl;

    @Value("${lebvest.set-password-token-secret}")
    private String tokenSecret;

    private static final String HMAC_ALGO = "HmacSHA256";

    @Transactional
    public ApiResponseDto<InvestorApplicationResponseDto> apply(
            InvestorApplicationRequestDto req
    ) {
        if (investorApplicationRepository.existsByEmail(req.email())) {
            log.warn("Duplicate investor application attempt for email={}", req.email());
            throw new ResourceConflictException(InvestorApplication.class, "email", req.email());
        }

        var application = InvestorApplication.builder()
                .firstname(req.firstname())
                .lastname(req.lastname())
                .email(req.email())
                .applicationStatus(InvestorApplicationStatus.PENDING)
                .build();

        application = investorApplicationRepository.saveAndFlush(application);
        Long requestId = application.getId();
        log.info("Investor application persisted id={}, email={}", requestId, application.getEmail());

        String identityKey = generateDocumentKey(req.identityDocumentName(), requestId);
        String addressKey = generateDocumentKey(req.addressProofDocumentName(), requestId);

        application.setIdentityDocumentKey(identityKey);
        application.setAddressProofDocumentKey(addressKey);
        investorApplicationRepository.save(application);

        var data = InvestorApplicationResponseDto.builder()
                .identityDocumentPresignedUrl(s3Service.presignUrl(identityKey).url().toString())
                .addressProofDocumentPresignedUrl(s3Service.presignUrl(addressKey).url().toString())
                .build();
        log.debug("Presigned URLs generated for application id={}", requestId);

        int year = LocalDate.now(clock).getYear();

        String investorStyles = stylesLoaderService.loadStyles("layout", "investor-application-confirmation");
        Map<String, Object> investorCtx = new HashMap<>();
        investorCtx.put("firstname", application.getFirstname());
        investorCtx.put("lastname", application.getLastname());
        investorCtx.put("email", application.getEmail());
        investorCtx.put("styles", investorStyles);
        investorCtx.put("year", year);
        String investorHtml = handlebarsRendererService.renderTemplate("investor-application-confirmation", investorCtx);

        Long applicationId= application.getId();
        String token = createToken(applicationId);
        String applicationUrl = frontendUrl + "/investor-applications/" + applicationId + "?token=" + token;

        // save the token in db
        SetPasswordToken setPasswordToken = SetPasswordToken
                .builder()
                .token(token)
                .application(application)
                .build();
        setPasswordTokenRepository.save(setPasswordToken);

        String adminStyles = stylesLoaderService.loadStyles("layout", "investor-application-notification");
        Map<String, Object> adminCtx = new HashMap<>();
        adminCtx.put("adminName", adminProperties.name());
        adminCtx.put("firstname", application.getFirstname());
        adminCtx.put("lastname", application.getLastname());
        adminCtx.put("email", application.getEmail());
        adminCtx.put("submittedAt", SUBMITTED_AT_FORMATTER.format(application.getCreatedAt()));
        adminCtx.put("applicationUrl", applicationUrl);
        adminCtx.put("styles", adminStyles);
        adminCtx.put("year", year);
        String adminHtml = handlebarsRendererService.renderTemplate("investor-application-notification", adminCtx);

        investorEventsProducer.publishInvestorApplicationToInvestorEmailEvent(
                new InvestorEvents.InvestorApplicationToInvestorEmailsEvent(
                        application.getEmail(),
                        investorHtml,
                        "Application Received — Lebvest"
                )
        );
        log.info("Investor confirmation email event published for email={}", application.getEmail());
        investorEventsProducer.publishInvestorApplicationToAdminEmailEvent(
                new AdminEvents.InvestorApplicationToAdminEmailsEvent(
                        adminProperties.email(),
                        adminHtml,
                        "New Investor Application — Lebvest"
                )
        );
        log.info("Admin notification email event published to={}", adminProperties.email());

        return ApiResponseDto.ok(HttpStatus.CREATED.value(), data);
    }

    public String generateDocumentKey(String filename, Long requestId) {
        if (filename == null || requestId == null) {
            throw new RuntimeException("filename and requestId must not be null");
        }
        LocalDate d = LocalDate.now(clock);
        String baseName = normalizeFilename(filename);
        String objectId = uuidSupplier.get().toString();
        return String.format(
                "%s/%04d/%02d/%02d/%d/%s_%s",
                INVESTOR_DOCUMENTS_PATH_PREFIX,
                d.getYear(),
                d.getMonthValue(),
                d.getDayOfMonth(),
                requestId,
                objectId,
                baseName
        );
    }

    public static String normalizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }

        // 1. Remove path traversal (both / and \)
        String name = filename.replace('\\', '/');
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }

        // 2. Normalize Unicode (avoid sneaky homoglyph attacks)
        name = Normalizer.normalize(name, Normalizer.Form.NFKC);

        // 3. Trim + lowercase
        name = name.trim().toLowerCase();

        // 4. Replace spaces with underscores
        name = name.replaceAll("\\s+", "_");

        // 5. Remove anything not safe (keep letters, numbers, dot, underscore, dash)
        name = name.replaceAll("[^a-z0-9._-]", "");

        // 6. Prevent hidden files like ".env"
        if (name.startsWith(".")) {
            name = "file" + name;
        }

        // 7. Handle extension safely (avoid "file.pdf.exe")
        int lastDot = name.lastIndexOf('.');
        if (lastDot != -1) {
            String base = name.substring(0, lastDot);
            String ext = name.substring(lastDot + 1);

            // Optional: enforce allowed extensions
            ext = ext.length() > 10 ? ext.substring(0, 10) : ext;

            name = base + "." + ext;
        }

        // 8. Limit length (S3 key sanity)
        int MAX_LENGTH = 100;
        if (name.length() > MAX_LENGTH) {
            name = name.substring(0, MAX_LENGTH);
        }

        // 9. Fallback if empty after cleaning
        if (name.isBlank()) {
            name = "file";
        }

        return name;
    }

    private String createToken(Long requestId) {
        long expiry = Instant.now(clock).plusSeconds(86400).getEpochSecond(); // 1 day
        String nonce = UUID.randomUUID().toString();

        String payload = requestId + "." + expiry + "." + nonce;
        String signature = hmacSha256Base64Url(payload, tokenSecret);

        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((payload + "." + signature).getBytes(StandardCharsets.UTF_8));
    }

    private String hmacSha256Base64Url(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGO
            );
            mac.init(keySpec);
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException("Could not sign token", e);
        }
    }
}
