package com.application.lebvest.models.events;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record InvestorSignupApplicationAdminNotificationEmail(

        @NotBlank
        String from,

        @NotBlank
        String to,

        @NotBlank
        String subject,

        @NotBlank
        String html
) {
}
