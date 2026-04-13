package com.application.lebvest.models.dtos;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MarkAsReadRequestDto(
        @NotEmpty
        List<Long> ids
) {
}
