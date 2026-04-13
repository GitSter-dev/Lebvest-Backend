package com.application.lebvest.controllers;

import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.InvestorApplicationRequestDto;
import com.application.lebvest.models.dtos.InvestorApplicationResponseDto;
import com.application.lebvest.models.dtos.SwaggerApiResponses;
import com.application.lebvest.services.InvestorApplicationService;
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
@RequestMapping("/investors/application")
@RequiredArgsConstructor
@Slf4j
public class InvestorApplicationController {

    private final InvestorApplicationService investorApplicationService;

    @PostMapping("")
    @Operation(summary = "Investor application to become an investor",
    description = """
            Investor request gets saved to the database,
            backend generates document keys for s3 to return presigned urls
            out of these keys for the client to upload them to S3. Sends emails
            one for confirmation for the investor and the other for the admin notification
            """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Investor application saved successfully",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponses.InvestorApplicationResponse.class)
                    )

            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Investor application with this email already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerApiResponses.InvestorApplicationConflictResponse.class)
                    )
            )
    })

    public ResponseEntity<ApiResponseDto<InvestorApplicationResponseDto>> apply(
            @Valid @RequestBody InvestorApplicationRequestDto req
            ) {
        log.info("Investor application request received for email={}", req.email());
        var response = investorApplicationService.apply(req);
        log.info("Investor application accepted for email={}", req.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
