package com.application.lebvest.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RabbitMessagingProperties.class)
@RequiredArgsConstructor
public class RabbitConfig {

    private final RabbitMessagingProperties messaging;

    @Bean
    public Queue investorApplicationToInvestorEmailsQueue() {
        return QueueBuilder
                .durable(messaging.queues().investorApplicationToInvestorEmails())
                .build();
    }

    @Bean
    public Queue investorApplicationToAdminEmailsQueue() {
        return QueueBuilder
                .durable(messaging.queues().investorApplicationToAdminEmails())
                .build();
    }

    @Bean
    public TopicExchange investorEventsExchange() {
        return ExchangeBuilder
                .topicExchange(messaging.exchanges().investorEvents())
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange adminEventsExchange() {
        return ExchangeBuilder
                .topicExchange(messaging.exchanges().adminEvents())
                .durable(true)
                .build();
    }

    @Bean
    public Binding investorApplicationToInvestorEmailsBinding() {
        return BindingBuilder
                .bind(investorApplicationToInvestorEmailsQueue())
                .to(investorEventsExchange())
                .with(messaging.routingKeys().investorApplicationToInvestorEmailSent());
    }

    @Bean
    public Queue adminNotificationsQueue() {
        return QueueBuilder
                .durable(messaging.queues().adminNotifications())
                .build();
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Binding investorApplicationToAdminEmailsBinding() {
        return BindingBuilder
                .bind(investorApplicationToAdminEmailsQueue())
                .to(adminEventsExchange())
                .with(messaging.routingKeys().investorApplicationToAdminEmailSent());
    }

    @Bean
    public Binding adminNotificationsBinding() {
        return BindingBuilder
                .bind(adminNotificationsQueue())
                .to(adminEventsExchange())
                .with(messaging.routingKeys().adminNotificationCreated());
    }
}
