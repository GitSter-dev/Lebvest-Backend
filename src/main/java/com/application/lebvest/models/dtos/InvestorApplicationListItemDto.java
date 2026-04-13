package com.application.lebvest.models.dtos;

import com.application.lebvest.models.enums.InvestorApplicationStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record InvestorApplicationListItemDto(
        Long id,
        String firstname,
        String lastname,
        String email,
        InvestorApplicationStatus applicationStatus,
        boolean hasIdentityDocument,
        boolean hasAddressProof,
        Instant createdAt,
        Instant updatedAt
) {
}
