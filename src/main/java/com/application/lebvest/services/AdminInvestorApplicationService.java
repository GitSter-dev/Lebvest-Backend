package com.application.lebvest.services;

import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.InvestorApplicationDecisionResponseDto;
import com.application.lebvest.models.entities.InvestorApplication;
import com.application.lebvest.models.enums.InvestorApplicationStatus;
import com.application.lebvest.models.exceptions.InvalidStateException;
import com.application.lebvest.models.exceptions.ResourceNotFoundException;
import com.application.lebvest.repositories.InvestorApplicationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminInvestorApplicationService {

    private final InvestorApplicationRepository investorApplicationRepository;
    private final SetPasswordTokenService setPasswordTokenService;
    private final InvestorApplicationEmailService investorApplicationEmailService;

    @Transactional
    public ApiResponseDto<InvestorApplicationDecisionResponseDto> acceptInvestorApplication(Long applicationId) {
        InvestorApplication application = getApplicationById(applicationId);
        ensurePending(application);

        application.setApplicationStatus(InvestorApplicationStatus.ACCEPTED);
        investorApplicationRepository.save(application);

        String setPasswordUrl = setPasswordTokenService.createSetPasswordUrl(application);
        investorApplicationEmailService.sendApprovalEmail(application, setPasswordUrl);
        log.info("Investor application approved id={}, email={}", application.getId(), application.getEmail());

        return ApiResponseDto.ok(HttpStatus.OK.value(), buildDecisionResponse(application));
    }

    @Transactional
    public ApiResponseDto<InvestorApplicationDecisionResponseDto> rejectInvestorApplication(Long applicationId) {
        InvestorApplication application = getApplicationById(applicationId);
        ensurePending(application);

        application.setApplicationStatus(InvestorApplicationStatus.REJECTED);
        investorApplicationRepository.save(application);
        setPasswordTokenService.invalidateTokensForApplication(application.getId());
        investorApplicationEmailService.sendRejectionEmail(application);
        log.info("Investor application rejected id={}, email={}", application.getId(), application.getEmail());

        return ApiResponseDto.ok(HttpStatus.OK.value(), buildDecisionResponse(application));
    }

    private InvestorApplication getApplicationById(Long applicationId) {
        return investorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(InvestorApplication.class, "id", applicationId));
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
