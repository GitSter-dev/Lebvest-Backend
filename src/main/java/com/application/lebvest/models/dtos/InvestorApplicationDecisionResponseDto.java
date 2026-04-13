package com.application.lebvest.models.dtos;

import com.application.lebvest.models.enums.InvestorApplicationStatus;
import lombok.Builder;

@Builder
public record InvestorApplicationDecisionResponseDto(
        Long applicationId,
        String email,
        InvestorApplicationStatus applicationStatus
) {
}
