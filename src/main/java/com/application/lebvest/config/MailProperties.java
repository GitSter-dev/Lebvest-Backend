package com.application.lebvest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "lebvest.mail")
@Getter
@Setter
public class MailProperties {

    private String from;
    private String adminEmail;
    private Subjects subjects = new Subjects();

    @Getter
    @Setter
    public static class Subjects {
        private String adminNotification;
        private String investorConfirmation;
    }
}
