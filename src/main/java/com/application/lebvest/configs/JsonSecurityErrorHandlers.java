package com.application.lebvest.configs;

import com.application.lebvest.models.dtos.ApiErrorDto;
import com.application.lebvest.models.dtos.ApiResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonSecurityErrorHandlers {

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden");
    }

    private void writeJson(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OBJECT_MAPPER.writeValue(
                response.getOutputStream(),
                ApiResponseDto.error(
                        status,
                        ApiErrorDto.builder().message(message).build()
                )
        );
    }
}
