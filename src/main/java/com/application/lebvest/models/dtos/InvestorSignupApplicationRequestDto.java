package com.application.lebvest.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InvestorSignupApplicationRequestDto(
        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String phoneNumber,

        @NotBlank
        String nationality,

        @NotBlank
        String countryOfResidence,

        /*
        File names that the backend will use to
        generate keys and from these keys generate
        presigned urls that the frontend will use
        to upload the documents to S3
         */
        @NotBlank
        String identityDocument,

        @NotBlank
        String proofOfResidenceDocument,

        @NotEmpty
        List<String> sourceOfFundsDocuments

) {
}
