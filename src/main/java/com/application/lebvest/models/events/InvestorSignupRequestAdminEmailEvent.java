package com.application.lebvest.models.events;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.springframework.context.annotation.Bean;

@Builder
public record InvestorSignupRequestAdminEmailEvent(
        @NotBlank
        String from,
        @NotBlank
        String subject,
        @NotBlank
        String html
) {
}
