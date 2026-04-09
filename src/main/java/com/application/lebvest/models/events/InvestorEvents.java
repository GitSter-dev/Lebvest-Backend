package com.application.lebvest.models.events;

import jakarta.validation.constraints.NotBlank;

/*
Data class to define all investor events records
 */
public class InvestorEvents {
    public record InvestorApplicationToInvestorEmailsEvent(
            @NotBlank String to, @NotBlank String html, @NotBlank String subject
    ) {}


}
