package com.application.lebvest.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record InvestorSetPasswordRequestDto(
        @NotBlank
        String token,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
