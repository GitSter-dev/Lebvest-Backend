package com.application.lebvest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
@RequiredArgsConstructor
public class RabbitConfig {

    private final QueueProperties queueProperties;

    @Bean
    public Queue investorSignupRequestEmailQueue() {
        return QueueBuilder.durable(
                queueProperties.getInvestorSignupRequestEmail())
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public TopicExchange investorTopicExchange() {
        return ExchangeBuilder.topicExchange(queueProperties.getInvestorTopicExchange())
                .durable(true)
                .build();
    }

    @Bean
    public Binding investorSignupRequestEmailBinding() {
        return BindingBuilder
                .bind(investorSignupRequestEmailQueue())
                .to(investorTopicExchange())
                .with(queueProperties.getInvestorSignupRequestEmail());
    }

    @Bean
    public Queue investorSignupRequestAdminEmailQueue() {
        return QueueBuilder
                .durable(queueProperties.getInvestorSignupRequestAdminEmail())
                .build();
    }

    @Bean
    public TopicExchange adminTopicExchange() {
        return ExchangeBuilder
                .topicExchange(queueProperties.getAdminTopicExchange())
                .durable(true)
                .build();
    }

    @Bean
    public Binding investorSignupRequestAdminEmailBinding() {
        return BindingBuilder
                .bind(investorSignupRequestAdminEmailQueue())
                .to(adminTopicExchange())
                .with(queueProperties.getInvestorSignupRequestAdminEmail());
    }

    @Bean
    public Queue investorSignupRequestDocumentsQueue() {
            return QueueBuilder
                    .durable(queueProperties.getInvestorSignupRequestDocuments())
                    .build();
    }

    @Bean
    public Binding investorSignupRequestDocumentsBinding() {
        return BindingBuilder
                .bind(investorSignupRequestDocumentsQueue())
                .to(investorTopicExchange())
                .with(queueProperties.getInvestorSignupRequestDocuments());
    }
}
