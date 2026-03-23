package com.application.lebvest.models.dtos;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record InvestorSignupRequestDto(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank String phoneNumber,

        @NotNull @Past LocalDate dateOfBirth,
        @NotBlank String nationality,
        @NotBlank String countryOfResidence,
        @NotBlank String address,

        // Economic Profile
        @NotBlank String occupation,
        @NotBlank String sourceOfFunds,
        @NotNull Long estimatedAnnualIncome,

        // Compliance
        @NotNull Boolean pep,
        @NotNull Boolean relativeOrFamilyPepStatus,
        @NotNull Boolean isUsPerson, // FATCA check
        @NotBlank String taxIdNumber,

        // Investment Profile
        @NotBlank String riskTolerance,
        @NotNull Integer yearsOfExperience
) {}