package com.application.lebvest.models.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Map;

@Builder
public record ApiErrorDto(
        @NotBlank
        String message,

        Map<String, Object> details
) {
}
