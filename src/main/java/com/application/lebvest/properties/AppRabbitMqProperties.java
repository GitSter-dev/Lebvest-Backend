package com.application.lebvest.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq")
@RequiredArgsConstructor
@Getter
public class AppRabbitMqProperties {

    private final Queues queues;
    private final Exchanges exchanges;

    @RequiredArgsConstructor
    @Getter
    public static class Queues {
        private final String investorSignupEmails;
        private final String adminEmails;
    }

    @RequiredArgsConstructor
    @Getter
    public static class Exchanges {
        private final String investorEmails;
        private final String adminEmails;
    }
}
