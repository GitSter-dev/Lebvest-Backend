package com.application.lebvest.services;

import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.InvestorApplicationDecisionResponseDto;
import com.application.lebvest.models.dtos.InvestorApplicationListItemDto;
import com.application.lebvest.models.entities.InvestorApplication;
import com.application.lebvest.models.enums.AdminNotificationType;
import com.application.lebvest.models.enums.InvestorApplicationStatus;
import com.application.lebvest.models.events.AdminEvents;
import com.application.lebvest.models.exceptions.InvalidStateException;
import com.application.lebvest.models.exceptions.ResourceNotFoundException;
import com.application.lebvest.producers.AdminEventsProducer;
import com.application.lebvest.repositories.InvestorApplicationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminInvestorApplicationService {

    private final InvestorApplicationRepository investorApplicationRepository;
    private final SetPasswordTokenService setPasswordTokenService;
    private final InvestorApplicationEmailService investorApplicationEmailService;
    private final AdminEventsProducer adminEventsProducer;

    @Transactional
    public ApiResponseDto<InvestorApplicationDecisionResponseDto> acceptInvestorApplication(Long applicationId) {
        InvestorApplication application = findApplicationById(applicationId);
        ensurePending(application);

        application.setApplicationStatus(InvestorApplicationStatus.ACCEPTED);
        investorApplicationRepository.save(application);

        String setPasswordUrl = setPasswordTokenService.createSetPasswordUrl(application);
        investorApplicationEmailService.sendApprovalEmail(application, setPasswordUrl);
        log.info("Investor application approved id={}, email={}", application.getId(), application.getEmail());

        adminEventsProducer.publishAdminNotificationEvent(new AdminEvents.AdminNotificationEvent(
                "Application accepted",
                application.getFirstname() + " " + application.getLastname() + " (" + application.getEmail() + ") was accepted.",
                AdminNotificationType.APPLICATION_ACCEPTED,
                Map.of("applicationId", application.getId(), "email", application.getEmail())
        ));

        return ApiResponseDto.ok(HttpStatus.OK.value(), buildDecisionResponse(application));
    }

    @Transactional
    public ApiResponseDto<InvestorApplicationDecisionResponseDto> rejectInvestorApplication(Long applicationId) {
        InvestorApplication application = findApplicationById(applicationId);
        ensurePending(application);

        application.setApplicationStatus(InvestorApplicationStatus.REJECTED);
        investorApplicationRepository.save(application);
        setPasswordTokenService.invalidateTokensForApplication(application.getId());
        investorApplicationEmailService.sendRejectionEmail(application);
        log.info("Investor application rejected id={}, email={}", application.getId(), application.getEmail());

        adminEventsProducer.publishAdminNotificationEvent(new AdminEvents.AdminNotificationEvent(
                "Application rejected",
                application.getFirstname() + " " + application.getLastname() + " (" + application.getEmail() + ") was rejected.",
                AdminNotificationType.APPLICATION_REJECTED,
                Map.of("applicationId", application.getId(), "email", application.getEmail())
        ));

        return ApiResponseDto.ok(HttpStatus.OK.value(), buildDecisionResponse(application));
    }

    public List<InvestorApplicationListItemDto> listApplications() {
        return investorApplicationRepository.findAll().stream()
                .map(this::toListItemDto)
                .toList();
    }

    public InvestorApplicationListItemDto getApplicationById(Long applicationId) {
        return toListItemDto(findApplicationById(applicationId));
    }

    private InvestorApplication findApplicationById(Long applicationId) {
        return investorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(InvestorApplication.class, "id", applicationId));
    }

    private InvestorApplicationListItemDto toListItemDto(InvestorApplication app) {
        return InvestorApplicationListItemDto.builder()
                .id(app.getId())
                .firstname(app.getFirstname())
                .lastname(app.getLastname())
                .email(app.getEmail())
                .applicationStatus(app.getApplicationStatus())
                .hasIdentityDocument(app.getIdentityDocumentKey() != null)
                .hasAddressProof(app.getAddressProofDocumentKey() != null)
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }

    private void ensurePending(InvestorApplication application) {
        if (application.getApplicationStatus() != InvestorApplicationStatus.PENDING) {
            throw new InvalidStateException(
                    "Investor application with id = "
                            + application.getId()
                            + " is already "
                            + application.getApplicationStatus()
            );
        }
    }

    private InvestorApplicationDecisionResponseDto buildDecisionResponse(InvestorApplication application) {
        return InvestorApplicationDecisionResponseDto.builder()
                .applicationId(application.getId())
                .email(application.getEmail())
                .applicationStatus(application.getApplicationStatus())
                .build();
    }
}
