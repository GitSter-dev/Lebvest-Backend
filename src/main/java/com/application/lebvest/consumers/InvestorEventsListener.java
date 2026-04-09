package com.application.lebvest.consumers;

import com.application.lebvest.models.events.InvestorEvents;
import com.application.lebvest.services.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class InvestorEventsListener {

    private final MailService mailService;

    @RabbitListener(queues = "${lebvest.messaging.queues.investor-application-to-investor-emails}")
    public void handleInvestorApplicationToInvestorEmailEvent(
            InvestorEvents.InvestorApplicationToInvestorEmailsEvent event
    ) {
        mailService.sendHtmlMail(
                event.to(), event.subject(), event.html()
        );
    }
}
