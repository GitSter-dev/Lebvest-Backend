package com.application.lebvest.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
@RequiredArgsConstructor
@Getter
public class AppMailProperties {

    private final String from;
    private final String adminEmail;
    private final Subjects subjects;
    private final Titles titles;

    @RequiredArgsConstructor
    @Getter
    public static class Subjects {
        private final String investorSignupConfirmation;
        private final String adminSignupNotification;
    }

    @RequiredArgsConstructor
    @Getter
    public static class Titles {
        private final String investorSignupConfirmation;
        private final String adminSignupNotification;
    }
}
