package com.application.lebvest.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender javaMailSender;
    private static final String MAIL_FROM = "noreply@lebvest.com";
    public void sendHtmlMail(String to, String subject, String html) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(MAIL_FROM);

            helper.setText(html, true);

            javaMailSender.send(message);
            log.info("HTML Email sent successfully to {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}", to, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
}