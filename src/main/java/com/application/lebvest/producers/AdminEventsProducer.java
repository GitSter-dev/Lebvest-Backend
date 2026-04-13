package com.application.lebvest.producers;

import com.application.lebvest.configs.RabbitMessagingProperties;
import com.application.lebvest.models.events.AdminEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminEventsProducer {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMessagingProperties messaging;

    public void publishAdminNotificationEvent(AdminEvents.AdminNotificationEvent event) {
        log.debug("Publishing admin notification event to exchange={} routingKey={}",
                messaging.exchanges().adminEvents(),
                messaging.routingKeys().adminNotificationCreated());
        rabbitTemplate.convertAndSend(
                messaging.exchanges().adminEvents(),
                messaging.routingKeys().adminNotificationCreated(),
                event
        );
    }
}
