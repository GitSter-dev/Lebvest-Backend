package com.application.lebvest.producers;

import com.application.lebvest.models.events.InvestorSignupApplicationAdminNotificationEmail;
import com.application.lebvest.models.events.InvestorSignupApplicationConfirmationEmail;
import com.application.lebvest.properties.AppRabbitMqProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class InvestorSignupEmailEventsProducer {

    private final RabbitTemplate rabbitTemplate;
    private final AppRabbitMqProperties rabbitMqProperties;

    public void publishInvestorSignupApplicationConfirmationEmail(
            @Valid InvestorSignupApplicationConfirmationEmail event) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchanges().getInvestorEmails(), "", event);
        log.debug("Published investor signup confirmation email for [{}]", event.to());
    }

    public void publishInvestorSignupApplicationAdminNotificationEmail(
            @Valid InvestorSignupApplicationAdminNotificationEmail event) {
        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchanges().getAdminEmails(), "", event);
        log.debug("Published admin signup notification email for [{}]", event.to());
    }
}
