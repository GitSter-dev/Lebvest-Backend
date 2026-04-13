package com.application.lebvest.controllers;

import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.InvestorApplicationDecisionResponseDto;
import com.application.lebvest.models.dtos.InvestorApplicationListItemDto;
import com.application.lebvest.models.dtos.SwaggerApiResponses;
import com.application.lebvest.services.AdminInvestorApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/investor-applications")
@RequiredArgsConstructor
@Slf4j
public class AdminInvestorApplicationController {

    private final AdminInvestorApplicationService adminInvestorApplicationService;

    @GetMapping
    @Operation(summary = "List all investor applications")
    public ResponseEntity<ApiResponseDto<List<InvestorApplicationListItemDto>>> listApplications() {
        var list = adminInvestorApplicationService.listApplications();
        return ResponseEntity.ok(ApiResponseDto.ok(HttpStatus.OK.value(), list));
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get investor application by ID")
    public ResponseEntity<ApiResponseDto<InvestorApplicationListItemDto>> getApplication(
            @PathVariable Long applicationId
    ) {
        var dto = adminInvestorApplicationService.getApplicationById(applicationId);
        return ResponseEntity.ok(ApiResponseDto.ok(HttpStatus.OK.value(), dto));
    }

    @PostMapping("/{applicationId}/accept")
    @Operation(
            summary = "Accept investor application",
            description = "Marks a pending investor application as accepted and sends the investor a set-password link."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Investor application accepted successfully",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponses.InvestorApplicationDecisionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Investor application not found",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponses.NotFoundResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Investor application is no longer pending",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponses.InvestorApplicationConflictResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponseDto<InvestorApplicationDecisionResponseDto>> acceptInvestorApplication(
            @PathVariable Long applicationId
    ) {
        log.info("Admin approval requested for investor application id={}", applicationId);
        var response = adminInvestorApplicationService.acceptInvestorApplication(applicationId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{applicationId}/reject")
    @Operation(
            summary = "Reject investor application",
            description = "Marks a pending investor application as rejected and notifies the investor by email."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Investor application rejected successfully",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponses.InvestorApplicationDecisionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Investor application not found",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponses.NotFoundResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Investor application is no longer pending",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerApiResponses.InvestorApplicationConflictResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponseDto<InvestorApplicationDecisionResponseDto>> rejectInvestorApplication(
            @PathVariable Long applicationId
    ) {
        log.info("Admin rejection requested for investor application id={}", applicationId);
        var response = adminInvestorApplicationService.rejectInvestorApplication(applicationId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
