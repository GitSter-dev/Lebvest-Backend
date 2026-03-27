package com.application.lebvest.listeners;

import com.application.lebvest.models.events.InvestorSignupApplicationAdminNotificationEmail;
import com.application.lebvest.models.events.InvestorSignupApplicationConfirmationEmail;
import com.application.lebvest.services.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class InvestorSignupEmailEventsListener {

    private final MailService mailService;

    @RabbitListener(queues = "${app.rabbitmq.queues.investor-signup-emails}")
    public void onInvestorSignupApplicationConfirmationEmail(
            @Valid InvestorSignupApplicationConfirmationEmail event) {
        mailService.sendHtmlMail(event.subject(), event.to(), event.html());
        log.info("Sent signup confirmation mail from queue to [{}]", event.to());
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.admin-emails}")
    public void onInvestorSignupApplicationAdminNotificationEmail(
            @Valid InvestorSignupApplicationAdminNotificationEmail event) {
        mailService.sendHtmlMail(event.subject(), event.to(), event.html());
        log.info("Sent admin signup notification mail from queue to [{}]", event.to());
    }
}
