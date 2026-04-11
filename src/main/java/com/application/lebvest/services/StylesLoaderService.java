package com.application.lebvest.services;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class StylesLoaderService {

    private static final String STYLES_BASE_PATH = "static/styles/";
    private final Map<String, String> stylesCache = new ConcurrentHashMap<>();

    public String loadStyles(String... stylesheets) {
        return Arrays.stream(stylesheets)
                .map(this::loadSingleStyle)
                .collect(Collectors.joining("\n"));
    }

    private String loadSingleStyle(String stylesheet) {
        return stylesCache.computeIfAbsent(stylesheet, name -> {
            try {
                ClassPathResource resource = new ClassPathResource(STYLES_BASE_PATH + name + ".css");
                return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load stylesheet: " + name, e);
            }
        });
    }
}
