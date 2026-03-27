package com.application.lebvest.config;

import com.application.lebvest.properties.AppRabbitMqProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * AMQP topology for prod. Gated on {@code spring.rabbitmq.host} (not {@code ConnectionFactory}) because
 * component-scanned configs run before {@code RabbitAutoConfiguration} registers the factory bean.
 */
@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.host")
@EnableConfigurationProperties(AppRabbitMqProperties.class)
public class RabbitConfig {

    @Bean
    public MessageConverter rabbitMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    public Queue investorSignupEmailsQueue(AppRabbitMqProperties props) {
        return new Queue(props.getQueues().getInvestorSignupEmails(), true);
    }

    @Bean
    public Queue adminEmailsQueue(AppRabbitMqProperties props) {
        return new Queue(props.getQueues().getAdminEmails(), true);
    }

    @Bean
    public FanoutExchange investorEmailsExchange(AppRabbitMqProperties props) {
        return new FanoutExchange(props.getExchanges().getInvestorEmails(), true, false);
    }

    @Bean
    public FanoutExchange adminEmailsExchange(AppRabbitMqProperties props) {
        return new FanoutExchange(props.getExchanges().getAdminEmails(), true, false);
    }

    @Bean
    public Binding investorSignupEmailsBinding(
            @Qualifier("investorSignupEmailsQueue") Queue investorSignupEmailsQueue,
            @Qualifier("investorEmailsExchange") FanoutExchange investorEmailsExchange) {
        return BindingBuilder.bind(investorSignupEmailsQueue).to(investorEmailsExchange);
    }

    @Bean
    public Binding adminEmailsBinding(
            @Qualifier("adminEmailsQueue") Queue adminEmailsQueue,
            @Qualifier("adminEmailsExchange") FanoutExchange adminEmailsExchange) {
        return BindingBuilder.bind(adminEmailsQueue).to(adminEmailsExchange);
    }
}
