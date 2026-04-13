package com.application.lebvest.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequestDto(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
