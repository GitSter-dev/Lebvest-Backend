package com.application.lebvest.consumers;

import com.application.lebvest.models.events.InvestorSignupRequestEmailEvent;
import com.application.lebvest.services.interfaces.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("prod")
public class InvestorEventsConsumer {

    private final MailService mailService;

    @RabbitListener(queues = "${rabbitmq.queues.investor-signup-request-email}")
    public void handleInvestorSignupRequestEmail(@Valid InvestorSignupRequestEmailEvent investorSignupRequestEmailEvent) {
        mailService.sendHtmlMail(investorSignupRequestEmailEvent.from(),
                investorSignupRequestEmailEvent.subject(),
                investorSignupRequestEmailEvent.html()
        );
    }




}
