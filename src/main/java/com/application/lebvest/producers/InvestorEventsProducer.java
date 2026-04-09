package com.application.lebvest.producers;

import com.application.lebvest.configs.RabbitMessagingProperties;
import com.application.lebvest.models.events.AdminEvents;
import com.application.lebvest.models.events.InvestorEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvestorEventsProducer {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMessagingProperties messaging;

    public void publishInvestorApplicationToInvestorEmailEvent(
            InvestorEvents.InvestorApplicationToInvestorEmailsEvent event
    ) {
        rabbitTemplate.convertAndSend(
                messaging.exchanges().investorEvents(),
                messaging.routingKeys().investorApplicationToInvestorEmailSent(),
                event
        );
    }

    public void publishInvestorApplicationToAdminEmailEvent(
            AdminEvents.InvestorApplicationToAdminEmailsEvent event
    ) {
        rabbitTemplate.convertAndSend(
                messaging.exchanges().adminEvents(),
                messaging.routingKeys().investorApplicationToAdminEmailSent(),
                event
        );
    }
}
