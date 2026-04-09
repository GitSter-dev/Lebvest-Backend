package com.application.lebvest.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record InvestorApplicationRequestDto(
        @NotBlank
        String firstname,

        @NotBlank
        String lastname,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String identityDocumentName,

        @NotBlank
        String addressProofDocumentName

) {
}
