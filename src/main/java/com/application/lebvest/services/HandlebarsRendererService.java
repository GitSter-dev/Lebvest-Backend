package com.application.lebvest.services;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.MapValueResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

            // Avoid default MemberValueResolver: Handlebars nests Collections.emptyMap() in
            // partial contexts, and reflecting on EmptyMap.isEmpty() fails on Java 21+.
            // Mail models are plain Map<String, Object>.
            Context ctx = Context.newBuilder(context)
                    .resolver(MapValueResolver.INSTANCE)
                    .build();
            return template.apply(ctx);

        } catch (IOException e) {
            throw new RuntimeException("Failed to render template: " + templateName, e);
        }
    }
}
