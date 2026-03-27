package com.application.lebvest.services;

import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.InvestorSignupApplicationRequestDto;
import com.application.lebvest.models.dtos.InvestorSignupApplicationResponseDto;
import com.application.lebvest.models.entities.InvestorSignupApplication;
import com.application.lebvest.models.exceptions.ResourceConflictException;
import com.application.lebvest.repositories.InvestorSignupApplicationRepository;
import com.application.lebvest.services.interfaces.InvestorAuthenticationService;
import com.application.lebvest.services.interfaces.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@Profile("dev")
@RequiredArgsConstructor
public class DevInvestorAuthenticationService implements InvestorAuthenticationService {

    private static final String UPLOADS_PATH_PREFIX = "pending";
    private static final String RESOURCE_TYPE = "investor-documents";
    private final InvestorSignupApplicationRepository investorSignupApplicationRepository;
    private final S3Service s3Service;
    private final InvestorSignupEmailService investorSignupEmailService;
    @Override
    public ApiResponseDto<InvestorSignupApplicationResponseDto> applyToSignup(
            InvestorSignupApplicationRequestDto requestDto) {
        if (investorSignupApplicationRepository.existsByEmailIgnoreCase(requestDto.email())) {
            throw new ResourceConflictException(InvestorSignupApplication.class, "email", requestDto.email());
        }

        // build the application from the basic fields
        InvestorSignupApplication application = InvestorSignupApplication
                .builder()
                .email(requestDto.email())
                .firstName(requestDto.firstName())
                .lastName(requestDto.lastName())
                .countryOfResidence(requestDto.countryOfResidence())
                .phoneNumber(requestDto.phoneNumber())
                .nationality(requestDto.nationality())
                .build();

        investorSignupApplicationRepository.save(application);
        log.info("Saved application with id: {}", application.getId());
        // generate the keys
        Long applicationId = application.getId();
        String identityDocumentKey = generateFullUploadPath(applicationId, requestDto.identityDocument());
        log.info("Identity document key: {}", identityDocumentKey);

        String proofOfResidenceKey = generateFullUploadPath(applicationId, requestDto.proofOfResidenceDocument());
        log.info("Proof of residence key: {}", proofOfResidenceKey);

        List<String> sourceOfFundsDocumentsKeys = requestDto.sourceOfFundsDocuments()
                .stream()
                .map(filename -> generateFullUploadPath(applicationId, filename))
                .collect(Collectors.toCollection(ArrayList::new));
        log.info("Source of funds document keys: {}", sourceOfFundsDocumentsKeys);

        application.setIdentityDocumentKey(identityDocumentKey);
        application.setProofOfResidenceDocumentKey(proofOfResidenceKey);
        application.setSourceOfFundsDocumentsKeys(sourceOfFundsDocumentsKeys);

        investorSignupApplicationRepository.saveAndFlush(application);

        try {
            investorSignupEmailService.sendConfirmationEmail(application);
            investorSignupEmailService.sendAdminNotificationEmail(application);
        } catch (IOException e) {
            log.error("Failed to send signup emails for application [{}]", application.getId(), e);
        }

        // generate presigned urls
        String identityDocumentPresignedUrl = s3Service.generatePresignedUrl(
                identityDocumentKey, getContentType(requestDto.identityDocument())
        ).url().toString();
        log.info("Identity document presigned url: {}", identityDocumentPresignedUrl);

        String proofOfResidencePresignedUrl = s3Service.generatePresignedUrl(
                proofOfResidenceKey, getContentType(requestDto.proofOfResidenceDocument())
        ).url().toString();
        log.info("Proof of residence presigned url: {}", proofOfResidencePresignedUrl);

        Map<String, String> sourceOfFundsDocumentsPresignedUrls =
                IntStream.range(0, sourceOfFundsDocumentsKeys.size())
                        .mapToObj(i -> Map.entry(
                                requestDto.sourceOfFundsDocuments().get(i),
                                sourceOfFundsDocumentsKeys.get(i)
                        ))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        ));
        log.info("Source of funds document presigned urls: {}", sourceOfFundsDocumentsPresignedUrls);
        return ApiResponseDto.ok(
                InvestorSignupApplicationResponseDto
                        .builder()
                        .identityDocumentPresignedUrl(identityDocumentPresignedUrl)
                        .proofOfResidenceDocumentPresignedUrl(proofOfResidencePresignedUrl)
                        .sourceOfFundsDocumentsPresignedUrls(sourceOfFundsDocumentsPresignedUrls)
                        .build(),
                HttpStatus.CREATED.value()
        );


    }

    private String generateFullUploadPath(Long entityId, String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        return String.format("%s/%s/%d/%s_%s",
                UPLOADS_PATH_PREFIX,
                RESOURCE_TYPE,
                entityId,
                uuid,
                normalizeFilename(originalFilename)); // replace spaces
    }

    private String normalizeFilename(String filename) {
        return filename.trim()
                .toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9._-]", "");
    }
    private String getContentType(String filename) {
        String contentType = URLConnection.guessContentTypeFromName(filename);
        return contentType != null ? contentType : "application/octet-stream";
    }
}
