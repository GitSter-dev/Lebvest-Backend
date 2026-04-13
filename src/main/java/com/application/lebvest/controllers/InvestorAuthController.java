package com.application.lebvest.controllers;

import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.InvestorSetPasswordRequestDto;
import com.application.lebvest.models.dtos.InvestorSetPasswordResponseDto;
import com.application.lebvest.models.dtos.SwaggerApiResponses;
import com.application.lebvest.services.InvestorAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/investors/auth")
@RequiredArgsConstructor
@Slf4j
public class InvestorAuthController {

    private final InvestorAuthService investorAuthService;

    @PostMapping("/set-password")
    @Operation(
            summary = "Set investor password",
            description = "Validates the approval token and creates the investor account with the provided password."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Investor password set successfully",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponses.InvestorSetPasswordResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation or token validation failed",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponses.BadRequestResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Investor account already exists or application is not approved",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponses.InvestorApplicationConflictResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponseDto<InvestorSetPasswordResponseDto>> setPassword(
            @Valid @RequestBody InvestorSetPasswordRequestDto req
    ) {
        log.info("Investor set password requested");
        var response = investorAuthService.setPassword(req);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
