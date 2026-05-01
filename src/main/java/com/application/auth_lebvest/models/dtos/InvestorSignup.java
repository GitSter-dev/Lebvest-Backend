package com.application.auth_lebvest.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;


@Builder
public record InvestorSignup(
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
        String proofOfAddressDocumentName,

        @NotBlank
        String selfieDocumentName
) {
}
