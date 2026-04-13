package com.application.lebvest.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lebvest.messaging")
public record RabbitMessagingProperties(
        Exchanges exchanges,
        Queues queues,
        RoutingKeys routingKeys
) {

    public record Exchanges(String investorEvents, String adminEvents) {
    }

    public record Queues(
            String investorApplicationToInvestorEmails,
            String investorApplicationToAdminEmails,
            String adminNotifications
    ) {
    }

    public record RoutingKeys(
            String investorApplicationToInvestorEmailSent,
            String investorApplicationToAdminEmailSent,
            String adminNotificationCreated
    ) {
    }
}
