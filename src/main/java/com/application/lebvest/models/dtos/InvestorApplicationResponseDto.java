package com.application.lebvest.models.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record InvestorApplicationResponseDto(
        @NotBlank
        String identityDocumentPresignedUrl,

        @NotBlank
        String addressProofDocumentPresignedUrl
) {
}
