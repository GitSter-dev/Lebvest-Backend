package com.application.lebvest.config;

import com.application.lebvest.properties.AppTemplateProperties;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HandlebarsConfig {

    private final AppTemplateProperties appTemplateProperties;
    @Bean
    public Handlebars handlebars() {
        String mailTemplatesPath = appTemplateProperties.getMailsPath();
        ClassPathTemplateLoader templateLoader = new ClassPathTemplateLoader(mailTemplatesPath, ".hbs");
        return new Handlebars(templateLoader);
    }
}
