package com.application.lebvest.services.email;

import com.application.lebvest.models.entities.InvestorSignupApplication;
import com.application.lebvest.services.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@Profile("dev")
@RequiredArgsConstructor
public class InvestorSignupEmailService {

    private final MailService mailService;
    private final InvestorSignupMailComposer investorSignupMailComposer;

    public void sendConfirmationEmail(InvestorSignupApplication application) throws IOException {
        var email = investorSignupMailComposer.composeInvestorSignupConfirmation(application);
        mailService.sendHtmlMail(email.subject(), email.to(), email.html());
        log.info("Sent signup confirmation email to [{}]", email.to());
    }

    public void sendAdminNotificationEmail(InvestorSignupApplication application) throws IOException {
        var email = investorSignupMailComposer.composeAdminSignupNotification(application);
        mailService.sendHtmlMail(email.subject(), email.to(), email.html());
        log.info("Sent admin notification email for application [{}]", application.getId());
    }
}
