package com.application.auth_lebvest.controllers;

import com.application.auth_lebvest.models.dtos.ApiResponses;
import com.application.auth_lebvest.models.dtos.ApiSuccessResponse;
import com.application.auth_lebvest.models.dtos.InvestorSignup;
import com.application.auth_lebvest.services.InvestorSignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/investor/signup")
@RequiredArgsConstructor
public class InvestorSignupController {

    private final InvestorSignupService investorSignupService;

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<ApiResponses.InvestorSignupData>> signup(
            @Valid @RequestBody InvestorSignup investorSignup
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(investorSignupService.signup(investorSignup));
    }
}
