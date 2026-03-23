package com.application.lebvest.consumers;

import com.application.lebvest.models.events.InvestorSignupRequestAdminEmailEvent;
import com.application.lebvest.services.interfaces.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("prod")
public class AdminEventsConsumer {

    private final MailService mailService;

    @RabbitListener(queues = "${rabbitmq.queues.investor-signup-request-admin-email}")
    public void handleInvestorSignupRequestAdminEmailEvent(
           @Valid InvestorSignupRequestAdminEmailEvent investorSignupRequestAdminEmailEvent
    ) {
        mailService.sendHtmlMail(
                investorSignupRequestAdminEmailEvent.from(),
                investorSignupRequestAdminEmailEvent.subject(),
                investorSignupRequestAdminEmailEvent.html()
        );
    }
}
