package com.application.lebvest.controllers;

import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.InvestorSignupApplicationRequestDto;
import com.application.lebvest.models.dtos.InvestorSignupApplicationResponseDto;
import com.application.lebvest.services.interfaces.InvestorAuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Investor Authentication", description = "Handles investor onboarding and authentication")
@Slf4j
@RestController
@RequestMapping("/investor/auth")
@RequiredArgsConstructor
public class InvestorAuthenticationController {

    private final InvestorAuthenticationService investorAuthenticationService;

    @Operation(
            summary = "Apply to sign up as an investor",
            description = """
                    Submits an investor signup application for review by admins.
                    Investor signup request is saved to the database, admin gets an
                    information email and the investor gets a confirmation email,
                    investor gets presigned urls of his documents in the
                    response to upload them to s3
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "409", description = "Application already exists for this email")
    })
    @PostMapping("/apply-to-signup")
    public ResponseEntity<ApiResponseDto<InvestorSignupApplicationResponseDto>> applyToSignup(
            @Valid @RequestBody InvestorSignupApplicationRequestDto investorSignupApplicationRequestDto
    ) {
        log.info("Received investor signup application: {}", investorAuthenticationService);
        ApiResponseDto<InvestorSignupApplicationResponseDto> response =
                investorAuthenticationService.applyToSignup(investorSignupApplicationRequestDto);
        return ResponseEntity.ok(response);
    }
}

