package com.application.lebvest.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

@ConfigurationProperties(prefix="rabbitmq.queues")
@Profile("prod")
@RequiredArgsConstructor
@Getter
public class QueueProperties {
    private final String investorSignupRequestEmail;
    private final String investorSignupRequestAdminEmail;
    private final String adminTopicExchange;
    private final String investorTopicExchange;
    private final String investorSignupRequestDocuments;
}
