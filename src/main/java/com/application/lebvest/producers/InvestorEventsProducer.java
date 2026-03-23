package com.application.lebvest.producers;

import com.application.lebvest.config.QueueProperties;
import com.application.lebvest.models.events.InvestorSignupRequestEmailEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class InvestorEventsProducer {

    private final RabbitTemplate rabbitTemplate;
    private final QueueProperties queueProperties;

    public void publishInvestorSignupRequestEmailEvent(InvestorSignupRequestEmailEvent investorSignupRequestEmailEvent) {
        rabbitTemplate.convertAndSend(queueProperties.getInvestorTopicExchange(),
                queueProperties.getInvestorSignupRequestEmail(),
                investorSignupRequestEmailEvent);
    }
}
