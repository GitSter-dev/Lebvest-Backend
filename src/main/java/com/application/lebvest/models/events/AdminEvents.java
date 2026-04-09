package com.application.lebvest.models.events;

import jakarta.validation.constraints.NotBlank;

public class AdminEvents {

    public record InvestorApplicationToAdminEmailsEvent(
            @NotBlank String to, @NotBlank String html, @NotBlank String subject
    ){}
}
