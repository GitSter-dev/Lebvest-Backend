package com.application.lebvest.services.impls;

import com.application.lebvest.services.interfaces.MailService;
import com.application.lebvest.models.exceptions.MailMessagingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@Profile("dev")
@RequiredArgsConstructor
public class DevMailService implements MailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendHtmlMail(String to, String subject, String html) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Failed to send email to recipient: ", e);
            throw new MailMessagingException("Failed to send email to recipient", to, e);
        }
    }
}