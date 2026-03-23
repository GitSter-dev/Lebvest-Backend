package com.application.lebvest.services.impls;

import com.application.lebvest.config.FrontendProperties;
import com.application.lebvest.config.MailProperties;

import com.application.lebvest.config.S3Properties;
import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.DocumentPresignedUrlsResponseDto;
import com.application.lebvest.models.dtos.InvestorSignupDocumentsDto;
import com.application.lebvest.models.dtos.InvestorSignupRequestDto;
import com.application.lebvest.models.entities.InvestorSignupRequest;
import com.application.lebvest.models.enums.InvestorSignupRequestStatus;
import com.application.lebvest.repositories.InvestorSignupRequestRepository;
import com.application.lebvest.services.interfaces.InvestorAuthenticationService;
import com.application.lebvest.services.interfaces.MailService;
import com.application.lebvest.models.exceptions.MailMessagingException;
import com.application.lebvest.utils.PathGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("dev")
public class DevInvestorAuthenticationService implements InvestorAuthenticationService {

    private final InvestorSignupRequestRepository investorSignupRequestRepository;
    private final ModelMapper modelMapper;
    private final TemplateRendererService templateRendererService;
    private final MailService mailService;
    private final MailProperties mailProperties;
    private final FrontendProperties frontendProperties;
    private final DevS3Service devS3Service;
    private final S3Properties s3Properties;




    @Override
    public ApiResponseDto<Map<String, DocumentPresignedUrlsResponseDto>> processInvestorSignupRequest(InvestorSignupRequestDto request, InvestorSignupDocumentsDto requestDocs) {
        // DTO Projection
        InvestorSignupRequest signupRequest = modelMapper.map(request, InvestorSignupRequest.class);
        log.info("Mapped signup request: {}", signupRequest);

        String prefix = "uploads/pending";
        // Build a map of document name + presigned url -> document's presigned URL
        // through mapping the document name to a destination path on s3 and presigning that path
        String nationalIdOrPassportKey = mapFileNameToPath(requestDocs.nationalIdOrPassport());
        String proofOfResidenceKey = mapFileNameToPath(requestDocs.proofOfResidence());
        List<String> addressProofKeys = requestDocs.addressProofDocuments()
                        .stream()
                        .map(this::mapFileNameToPath)
                        .toList();

        List<String> sourceOfFundsKeys = requestDocs.sourceOfFundsDocuments()
                .stream()
                .map(this::mapFileNameToPath)
                .toList();

        signupRequest.setStatus(InvestorSignupRequestStatus.PENDING);
        //set file keys
        signupRequest.setAddressProofDocumentPaths(addressProofKeys);
        signupRequest.setProofOfResidencePath(proofOfResidenceKey);
        signupRequest.setNationalIdOrPassportPath(nationalIdOrPassportKey);
        signupRequest.setSourceOfFundsDocumentPaths(sourceOfFundsKeys);

        Map<String, String> addressProofPresignedUrls = IntStream.range(0, addressProofKeys.size())
                .mapToObj(i -> Map.entry(
                        requestDocs.addressProofDocuments().get(i),
                        devS3Service.generatePresignedUrlResponse(
                                addressProofKeys.get(i),
                                requestDocs.addressProofDocuments().get(i) )
                ))
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue)
                );

        Map<String, String> sourceOfFundsPresignedUrls = IntStream.range(0, sourceOfFundsKeys.size())
                .mapToObj(i -> Map.entry(
                        requestDocs.sourceOfFundsDocuments().get(i),
                        devS3Service.generatePresignedUrlResponse(
                                sourceOfFundsKeys.get(i), requestDocs.sourceOfFundsDocuments().get(i) )
                ))
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue)
                );

        DocumentPresignedUrlsResponseDto presignedUrlsResponseDto = DocumentPresignedUrlsResponseDto
                .builder().nationalIdOrPassportPresignedUrl(devS3Service.generatePresignedUrlResponse(nationalIdOrPassportKey, requestDocs.nationalIdOrPassport()))
                        .proofOfResidencePresignedUrl(devS3Service.generatePresignedUrlResponse(proofOfResidenceKey, requestDocs.proofOfResidence()))
                                .addressProofDocumentsPresignedUrls(addressProofPresignedUrls)
                                        .sourceOfFundsDocumentsPresignedUrls(sourceOfFundsPresignedUrls)
                .build();


        investorSignupRequestRepository.saveAndFlush(signupRequest);
        log.info("Investor signup request with id: {}saved successfully", signupRequest.getId());

        // Send email to both investor and admin
        try {
            String emailStyles = templateRendererService.loadStyles("email-base.css", "investor-signup.css");
            // Admin email
            Map<String, Object> adminContext =
                    buildAdminContext(request, signupRequest, emailStyles);

            String adminSubject = mailProperties.getSubjects().getAdminNotification()
                    .replace("{firstName}", request.firstName())
                    .replace("{lastName}", request.lastName());

            sendTemplatedEmail(
                    mailProperties.getAdminEmail(),
                    adminSubject,
                    "investor-signup-request-admin",
                    adminContext
            );

            log.info("Email sent to admin successfully");

            // Investor email
            Map<String, Object> investorContext =
                    buildInvestorContext(request, emailStyles);

            sendTemplatedEmail(
                    request.email(),
                    mailProperties.getSubjects().getInvestorConfirmation(),
                    "investor-signup-request",
                    investorContext
            );

            log.info("Email sent to investor successfully");

        } catch (IOException e) {
            log.error("Failed to process investor signup email templates", e);
            throw new MailMessagingException(
                    "Failed to process email templates for signup request",
                    request.email(),
                    e
            );
        }

        return ApiResponseDto.ok(Map.of(
                "presignedUrls", presignedUrlsResponseDto
        ), 201);
    }

    private String mapFileNameToPath(String fileName) {
        String prefix = "uploads/pending";

        return PathGenerator.generateUserDocumentPath(prefix, null, StringUtils.getFilenameExtension(fileName));
    }

    private void sendTemplatedEmail(
            String to,
            String subject,
            String templateName,
            Map<String, Object> context
    ) throws IOException {

        String html = templateRendererService.renderHbsTemplate(templateName, context);

        mailService.sendHtmlMail(
                to,
                subject,
                html
        );
    }

    private Map<String, Object> baseEmailContext(String emailStyles) {
        return Map.of(
                "styles", emailStyles,
                "year", java.time.Year.now().getValue()
        );
    }
    private Map<String, Object> buildAdminContext(
            InvestorSignupRequestDto request,
            InvestorSignupRequest signupRequest,
            String emailStyles
    ) {
        return Stream.concat(
                baseEmailContext(emailStyles).entrySet().stream(),
                Stream.of(
                        Map.entry("firstName", request.firstName()),
                        Map.entry("lastName", request.lastName()),
                        Map.entry("email", request.email()),
                        Map.entry("phoneNumber", request.phoneNumber()),
                        Map.entry("dateOfBirth", request.dateOfBirth()),
                        Map.entry("nationality", request.nationality()),
                        Map.entry("countryOfResidence", request.countryOfResidence()),
                        Map.entry("address", request.address()),
                        Map.entry("occupation", request.occupation()),
                        Map.entry("sourceOfFunds", request.sourceOfFunds()),
                        Map.entry("estimatedAnnualIncome", request.estimatedAnnualIncome()),
                        Map.entry("pep", request.pep()),
                        Map.entry("relativeOrFamilyPepStatus", request.relativeOrFamilyPepStatus()),
                        Map.entry("isUsPerson", request.isUsPerson()),
                        Map.entry("taxIdNumber", request.taxIdNumber()),
                        Map.entry("riskTolerance", request.riskTolerance()),
                        Map.entry("yearsOfExperience", request.yearsOfExperience()),
                        Map.entry("reviewUrl", frontendProperties.getAdminUrl() + "/requests/" + signupRequest.getId()),
                        Map.entry("submittedAt", signupRequest.getCreatedAt().toString())
                )
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, Object> buildInvestorContext(
            InvestorSignupRequestDto request,
            String emailStyles
    ) {
        return Stream.concat(
                baseEmailContext(emailStyles).entrySet().stream(),
                Stream.of(
                        Map.entry("firstName", request.firstName()),
                        Map.entry("lastName", request.lastName()),
                        Map.entry("email", request.email()),
                        Map.entry("phoneNumber", request.phoneNumber()),
                        Map.entry("nationality", request.nationality()),
                        Map.entry("riskTolerance", request.riskTolerance())
                )
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


}
