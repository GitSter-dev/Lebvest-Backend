package com.application.lebvest.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.List;
import java.util.Map;

@Builder
public record DocumentPresignedUrlsResponseDto(
        @NotBlank
        String nationalIdOrPassportPresignedUrl,

        @NotBlank
        String proofOfResidencePresignedUrl,

        @NotEmpty
        Map<@NotBlank String,@NotBlank String> addressProofDocumentsPresignedUrls,

        @NotEmpty
        Map<@NotBlank String,@NotBlank String> sourceOfFundsDocumentsPresignedUrls
) {
}
