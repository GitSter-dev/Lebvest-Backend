package com.application.lebvest.services;

import com.application.lebvest.properties.AppMailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {


    private final JavaMailSender javaMailSender;
    private final AppMailProperties appMailProperties;

    public void sendHtmlMail(String subject, String to, String html) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(appMailProperties.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            javaMailSender.send(message);
            log.debug("Sent HTML mail to [{}] subject [{}]", to, subject);
        } catch (MessagingException e) {
            throw new MailPreparationException("Failed to prepare HTML mail", e);
        }
    }

}
