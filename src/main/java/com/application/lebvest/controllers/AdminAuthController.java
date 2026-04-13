package com.application.lebvest.controllers;

import com.application.lebvest.models.dtos.AdminAuthTokenPayloadDto;
import com.application.lebvest.models.dtos.AdminLoginRequestDto;
import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.services.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    @Operation(summary = "Admin login", description = "Issues a JWT for the admin SPA.")
    public ResponseEntity<ApiResponseDto<AdminAuthTokenPayloadDto>> login(
            @Valid @RequestBody AdminLoginRequestDto request
    ) {
        return ResponseEntity.ok(adminAuthService.login(request));
    }
}
