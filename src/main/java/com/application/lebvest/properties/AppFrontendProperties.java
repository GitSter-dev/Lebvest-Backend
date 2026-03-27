package com.application.lebvest.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.frontend")
@RequiredArgsConstructor
@Getter
public class AppFrontendProperties {

    private final String baseUrl;
}
