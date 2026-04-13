package com.application.lebvest.models.dtos;

import com.application.lebvest.models.enums.AdminNotificationType;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
public record AdminNotificationDto(
        Long id,
        String title,
        String content,
        AdminNotificationType type,
        boolean read,
        Map<String, Object> data,
        Instant createdAt
) {
}
