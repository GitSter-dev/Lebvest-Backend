package com.application.lebvest.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lebvest.admin")
public record AdminProperties(String email, String name) {
}
