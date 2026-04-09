package com.application.lebvest.consumers;

import com.application.lebvest.models.events.AdminEvents;
import com.application.lebvest.services.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminEventsListener {

    private final MailService mailService;

    @RabbitListener(queues = "${lebvest.messaging.queues.investor-application-to-admin-emails}")
    public void handleInvestorApplicationToAdminEmailEvent(
            AdminEvents.InvestorApplicationToAdminEmailsEvent event
    ) {
        mailService.sendHtmlMail(
                event.to(), event.subject(), event.html()
        );
    }
}
