package com.application.lebvest.controllers;

import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.DocumentPresignedUrlsResponseDto;
import com.application.lebvest.models.dtos.InvestorSignupRequestWithDocsDto;
import com.application.lebvest.services.interfaces.InvestorAuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth/investor")
@RequiredArgsConstructor
public class InvestorAuthenticationController {
    private final InvestorAuthenticationService investorAuthenticationService;

    @PostMapping("/request-signup")
    public ResponseEntity<ApiResponseDto<Map<String, DocumentPresignedUrlsResponseDto>>> processInvestorSignupRequest(
            @Valid @RequestBody InvestorSignupRequestWithDocsDto requestWrapper) {

        log.info("Signup Request: {}", requestWrapper.request());
        ApiResponseDto<Map<String, DocumentPresignedUrlsResponseDto>> response = investorAuthenticationService.processInvestorSignupRequest(
                requestWrapper.request(), requestWrapper.documents()
        );
        return ResponseEntity.ok(response);
    }

}
