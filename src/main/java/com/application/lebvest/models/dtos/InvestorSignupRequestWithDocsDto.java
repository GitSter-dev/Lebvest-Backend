package com.application.lebvest.models.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record InvestorSignupRequestWithDocsDto(
        @Valid @NotNull InvestorSignupRequestDto request,
        @Valid @NotNull InvestorSignupDocumentsDto documents
) {}
