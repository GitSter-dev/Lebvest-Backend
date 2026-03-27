package com.application.lebvest.services;

import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.InvestorSignupApplicationRequestDto;
import com.application.lebvest.models.dtos.InvestorSignupApplicationResponseDto;
import com.application.lebvest.models.entities.InvestorSignupApplication;
import com.application.lebvest.models.exceptions.ResourceConflictException;
import com.application.lebvest.producers.InvestorSignupEmailEventsProducer;
import com.application.lebvest.repositories.InvestorSignupApplicationRepository;
import com.application.lebvest.services.email.InvestorSignupMailComposer;
import com.application.lebvest.services.interfaces.InvestorAuthenticationService;
import com.application.lebvest.services.interfaces.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Profile("prod")
@Service
@RequiredArgsConstructor
public class ProdInvestorAuthenticationService implements InvestorAuthenticationService {

    private static final String UPLOADS_PATH_PREFIX = "pending";
    private static final String RESOURCE_TYPE = "investor-documents";
    private final InvestorSignupApplicationRepository investorSignupApplicationRepository;
    private final S3Service s3Service;
    private final InvestorSignupMailComposer investorSignupMailComposer;
    private final InvestorSignupEmailEventsProducer investorSignupEmailEventsProducer;
    @Override
    public ApiResponseDto<InvestorSignupApplicationResponseDto> applyToSignup(InvestorSignupApplicationRequestDto requestDto) {
        if (investorSignupApplicationRepository.existsByEmailIgnoreCase(requestDto.email())) {
            throw new ResourceConflictException(InvestorSignupApplication.class, "email", requestDto.email());
        }
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
        Long applicationId = application.getId();
        String identityDocumentKey = generateFullUploadPath(applicationId, requestDto.identityDocument());
        String proofOfResidenceKey = generateFullUploadPath(applicationId, requestDto.proofOfResidenceDocument());
        List<String> sourceOfFundsDocumentsKeys = requestDto.sourceOfFundsDocuments()
                .stream()
                .map(filename -> generateFullUploadPath(applicationId, filename))
                .collect(Collectors.toCollection(ArrayList::new));
        application.setIdentityDocumentKey(identityDocumentKey);
        application.setProofOfResidenceDocumentKey(proofOfResidenceKey);
        application.setSourceOfFundsDocumentsKeys(sourceOfFundsDocumentsKeys);

        investorSignupApplicationRepository.saveAndFlush(application);

        try {
            investorSignupEmailEventsProducer.publishInvestorSignupApplicationConfirmationEmail(
                    investorSignupMailComposer.composeInvestorSignupConfirmation(application));
            investorSignupEmailEventsProducer.publishInvestorSignupApplicationAdminNotificationEmail(
                    investorSignupMailComposer.composeAdminSignupNotification(application));
        } catch (IOException e) {
            log.error("Failed to queue signup emails for application [{}]", application.getId(), e);
        }

        String identityDocumentPresignedUrl = s3Service.generatePresignedUrl(
                identityDocumentKey, getContentType(requestDto.identityDocument())
        ).url().toString();

        String proofOfResidencePresignedUrl = s3Service.generatePresignedUrl(
                proofOfResidenceKey, getContentType(requestDto.proofOfResidenceDocument())
        ).url().toString();

        Map<String, String> sourceOfFundsDocumentsPresignedUrls =
                IntStream.range(0, sourceOfFundsDocumentsKeys.size())
                        .mapToObj(i -> Map.entry(
                                requestDto.sourceOfFundsDocuments().get(i),
                                sourceOfFundsDocumentsKeys.get(i)
                        ))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return ApiResponseDto.ok(
                InvestorSignupApplicationResponseDto.builder()
                        .identityDocumentPresignedUrl(identityDocumentPresignedUrl)
                        .proofOfResidenceDocumentPresignedUrl(proofOfResidencePresignedUrl)
                        .sourceOfFundsDocumentsPresignedUrls(sourceOfFundsDocumentsPresignedUrls)
                        .build(),
                HttpStatus.CREATED.value());
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
