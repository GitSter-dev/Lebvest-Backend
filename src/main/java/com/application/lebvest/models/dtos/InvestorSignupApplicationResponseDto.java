package com.application.lebvest.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import org.hibernate.validator.constraints.BitcoinAddress;

import java.util.Map;

@Builder
public record InvestorSignupApplicationResponseDto(
        @NotBlank
        String identityDocumentPresignedUrl,

        @NotBlank
        String proofOfResidenceDocumentPresignedUrl,

        @NotEmpty
        Map<@NotBlank String,@NotBlank String> sourceOfFundsDocumentsPresignedUrls
) {
}
