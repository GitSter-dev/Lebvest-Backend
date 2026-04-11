package com.application.lebvest.configs;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandlebarsConfig {

    @Bean
    public Handlebars handlebars() {
        ClassPathTemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates/mails");
        loader.setSuffix(".hbs");
        return new Handlebars(loader);
    }
}