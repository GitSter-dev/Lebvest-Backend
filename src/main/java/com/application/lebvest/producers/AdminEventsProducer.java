package com.application.lebvest.producers;

import com.application.lebvest.config.QueueProperties;
import com.application.lebvest.models.events.InvestorSignupRequestAdminEmailEvent;
import jakarta.validation.constraints.Past;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("prod")
public class AdminEventsProducer {

    private final RabbitTemplate rabbitTemplate;
    private final QueueProperties queueProperties;

    public void publishInvestorSignupRequestAdminEmailEvent(
            InvestorSignupRequestAdminEmailEvent investorSignupRequestAdminEmailEvent
    ) {
        rabbitTemplate.convertAndSend(queueProperties.getAdminTopicExchange(),
                queueProperties.getInvestorSignupRequestAdminEmail(),
                investorSignupRequestAdminEmailEvent);
    }
}
