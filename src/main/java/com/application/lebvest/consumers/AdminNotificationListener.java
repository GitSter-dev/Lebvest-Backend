package com.application.lebvest.consumers;

import com.application.lebvest.models.events.AdminEvents;
import com.application.lebvest.services.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationListener {

    private final AdminNotificationService adminNotificationService;

    @RabbitListener(queues = "${lebvest.messaging.queues.admin-notifications}")
    public void handleAdminNotificationEvent(AdminEvents.AdminNotificationEvent event) {
        log.debug("Received admin notification event type={}", event.type());
        adminNotificationService.createAndBroadcast(
                event.title(),
                event.content(),
                event.type(),
                event.data()
        );
    }
}
