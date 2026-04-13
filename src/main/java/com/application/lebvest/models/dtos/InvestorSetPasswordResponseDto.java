package com.application.lebvest.models.dtos;

import lombok.Builder;

@Builder
public record InvestorSetPasswordResponseDto(
        Long accountId,
        String email
) {
}
