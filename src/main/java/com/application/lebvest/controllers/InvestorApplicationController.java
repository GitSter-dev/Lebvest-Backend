package com.application.lebvest.controllers;

import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.InvestorApplicationRequestDto;
import com.application.lebvest.models.dtos.InvestorApplicationResponseDto;
import com.application.lebvest.services.InvestorApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/investors/application")
@RequiredArgsConstructor
@Slf4j
public class InvestorApplicationController {

    private final InvestorApplicationService investorApplicationService;

    @PostMapping("")
    public ResponseEntity<ApiResponseDto<InvestorApplicationResponseDto>> apply(
            @Valid @RequestBody InvestorApplicationRequestDto req
            ) {
        log.info("Investor application request received for email={}", req.email());
        var response = investorApplicationService.apply(req);
        log.info("Investor application accepted for email={}", req.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
