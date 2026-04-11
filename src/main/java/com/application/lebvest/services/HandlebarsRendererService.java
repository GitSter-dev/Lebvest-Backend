package com.application.lebvest.services;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class HandlebarsRendererService {
    private final Handlebars handlebars;
    // template name to template mapping
    private final Map<String, Template> templateCache = new ConcurrentHashMap<>();

    public String renderTemplate(String templateName, Map<String, Object> context) {
        try {
            Template template = templateCache.computeIfAbsent(templateName, name -> {
                try {
                    return handlebars.compile(name);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to compile template: " + name, e);
                }
            });

            return template.apply(context);

        } catch (IOException e) {
            throw new RuntimeException("Failed to render template: " + templateName, e);
        }
    }
}
