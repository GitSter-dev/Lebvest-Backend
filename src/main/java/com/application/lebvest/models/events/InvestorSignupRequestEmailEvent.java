package com.application.lebvest.models.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record InvestorSignupRequestEmailEvent(
        @NotBlank
        String from,
        @NotBlank
        String subject,
        @NotBlank
        String html
) {
}
