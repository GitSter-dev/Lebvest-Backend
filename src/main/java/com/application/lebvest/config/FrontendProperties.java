package com.application.lebvest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "lebvest.frontend")
@Getter
@Setter
public class FrontendProperties {

    private String frontendUrl;
    private String adminUrl;
}
