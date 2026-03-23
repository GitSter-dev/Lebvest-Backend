package com.application.lebvest.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record InvestorSignupDocumentsDto(
        @NotBlank
        String nationalIdOrPassport,

        @NotBlank
        String proofOfResidence,

        @NotEmpty
        List<@NotBlank String> addressProofDocuments,

        @NotEmpty
        List<@NotBlank String> sourceOfFundsDocuments
) {

}