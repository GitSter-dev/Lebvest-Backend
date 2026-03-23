package com.application.lebvest.services.impls;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TemplateRendererService {

    private final Handlebars handlebars;
    private final Map<String, String> stylesCache = new ConcurrentHashMap<>();
    public TemplateRendererService() {
        this.handlebars = new Handlebars(new ClassPathTemplateLoader("/templates/mails", ".hbs"));
    }

    public String renderHbsTemplate(String templateName, Map<String, Object> context) throws IOException {
        Template template = handlebars.compile(templateName);

        Context ctx = Context.newBuilder(context).build();

        return template.apply(ctx);
    }

    public String loadStyles(String... cssPaths) throws IOException {
        StringBuilder styles = new StringBuilder();
        for (String path : cssPaths) {
            styles.append(stylesCache.computeIfAbsent(path, this::readStyleUnchecked));
            styles.append("\n");
        }
        return styles.toString();
    }

    private String readStyleUnchecked(String path) {
        try (InputStream is = getClass().getResourceAsStream("/static/styles/" + path)) {
            if (is == null) throw new RuntimeException("Stylesheet not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read style: ", e);
            throw new RuntimeException(e);
        }
    }

}
