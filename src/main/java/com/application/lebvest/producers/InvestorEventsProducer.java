package com.application.lebvest.producers;

import com.application.lebvest.configs.RabbitMessagingProperties;
import com.application.lebvest.models.events.AdminEvents;
import com.application.lebvest.models.events.InvestorEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvestorEventsProducer {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMessagingProperties messaging;

    public void publishInvestorApplicationToInvestorEmailEvent(
            InvestorEvents.InvestorApplicationToInvestorEmailsEvent event
    ) {
        publishInvestorEmailEvent(event);
    }

    public void publishInvestorEmailEvent(
            InvestorEvents.InvestorApplicationToInvestorEmailsEvent event
    ) {
        log.debug("Publishing investor email event to exchange={} routingKey={}",
                messaging.exchanges().investorEvents(),
                messaging.routingKeys().investorApplicationToInvestorEmailSent());
        rabbitTemplate.convertAndSend(
                messaging.exchanges().investorEvents(),
                messaging.routingKeys().investorApplicationToInvestorEmailSent(),
                event
        );
    }

    public void publishInvestorApplicationToAdminEmailEvent(
            AdminEvents.InvestorApplicationToAdminEmailsEvent event
    ) {
        publishAdminEmailEvent(event);
    }

    public void publishAdminEmailEvent(
            AdminEvents.InvestorApplicationToAdminEmailsEvent event
    ) {
        log.debug("Publishing admin email event to exchange={} routingKey={}",
                messaging.exchanges().adminEvents(),
                messaging.routingKeys().investorApplicationToAdminEmailSent());
        rabbitTemplate.convertAndSend(
                messaging.exchanges().adminEvents(),
                messaging.routingKeys().investorApplicationToAdminEmailSent(),
                event
        );
    }
}
