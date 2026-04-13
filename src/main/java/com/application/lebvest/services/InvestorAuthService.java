package com.application.lebvest.services;

import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.InvestorSetPasswordRequestDto;
import com.application.lebvest.models.dtos.InvestorSetPasswordResponseDto;
import com.application.lebvest.models.entities.InvestorAccount;
import com.application.lebvest.models.entities.InvestorApplication;
import com.application.lebvest.models.entities.SetPasswordToken;
import com.application.lebvest.models.enums.InvestorApplicationStatus;
import com.application.lebvest.models.exceptions.InvalidStateException;
import com.application.lebvest.models.exceptions.ResourceConflictException;
import com.application.lebvest.repositories.InvestorAccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestorAuthService {

    private final InvestorAccountRepository investorAccountRepository;
    private final SetPasswordTokenService setPasswordTokenService;
    private final PasswordEncoder passwordEncoder;

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
}
