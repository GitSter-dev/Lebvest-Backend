package com.application.lebvest.config;

import org.springframework.boot.http.converter.autoconfigure.ServerHttpMessageConvertersCustomizer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConverters.ServerBuilder;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * MVC JSON uses Spring Framework 7 {@link JacksonJsonHttpMessageConverter} backed by
 * Jackson 3.x {@link JsonMapper}. Feature tuning uses {@link JsonMapperBuilderCustomizer}
 * (Jackson 3 date/time features are not bound the same way as Jackson 2 under {@code spring.jackson}).
 */
@Configuration
public class JacksonHttpMessageConverterConfiguration {

    @Bean
    JsonMapperBuilderCustomizer lebvestJsonMapperBuilderCustomizer() {
        return builder -> builder.configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    ServerHttpMessageConvertersCustomizer lebvestJacksonJsonHttpMessageConverter(JsonMapper jsonMapper) {
        JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(jsonMapper);
        return (ServerBuilder builder) -> builder.withJsonConverter(converter);
    }
}
