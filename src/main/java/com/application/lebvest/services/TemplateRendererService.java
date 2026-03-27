package com.application.lebvest.services;

import com.application.lebvest.properties.AppTemplateProperties;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateRendererService {

    private final Handlebars handlebars;
    private final AppTemplateProperties templateProperties;
    private final ResourceLoader resourceLoader;

    /**
     *
     * @param context Variables to be injected in the email including styles
     * @return the rendered hbs template with the variables replaced
     */
    public String renderMailTemplate(
            String templateName,
            Map<String, ?> context
    ) {
        try {
            Template template = handlebars.compile(templateName);
            Context ctx = Context.newBuilder(context).build();
            return template.apply(ctx);
        } catch (IOException e) {
            log.error("Failed to render email template:", e);
            throw new RuntimeException(e);
        }
    }

    public String loadStyle(String stylesheetName) throws IOException {
        String fileName = stylesheetName.endsWith(".css") ? stylesheetName : stylesheetName + ".css";
        String classpathLocation =
                ResourceUtils.CLASSPATH_URL_PREFIX + templateProperties.getStylesPath() + "/" + fileName;
        Resource resource = resourceLoader.getResource(classpathLocation);
        if (!resource.exists()) {
            throw new IOException("Stylesheet not found: " + classpathLocation);
        }
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public String loadStyles(String... stylesheets) throws IOException {
        StringBuilder combinedStyles = new StringBuilder();

        for (String stylesheet : stylesheets) {
                combinedStyles.append(loadStyle(stylesheet)).append("\n");
        }

        return combinedStyles.toString();
    }

}
