package com.application.lebvest.models.events;

import com.application.lebvest.models.enums.AdminNotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class AdminEvents {

    public record InvestorApplicationToAdminEmailsEvent(
            @NotBlank String to, @NotBlank String html, @NotBlank String subject
    ){}

    public record AdminNotificationEvent(
            @NotBlank String title,
            @NotBlank String content,
            @NotNull AdminNotificationType type,
            Map<String, Object> data
    ){}
}
