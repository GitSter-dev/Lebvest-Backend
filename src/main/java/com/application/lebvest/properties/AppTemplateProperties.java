package com.application.lebvest.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.template")
@RequiredArgsConstructor
@Getter
public class AppTemplateProperties {
    private final String mailsPath;
    private final String stylesPath;

}
