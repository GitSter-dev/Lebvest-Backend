package com.application.auth_lebvest.models.dtos;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApiErrorResponse {

    private String message;
    private int statusCode;
    private Map<String, Object> details = new HashMap<>();

    public static ApiErrorResponse of(String message, int statusCode) {
        return ApiErrorResponse.builder()
                .message(message)
                .statusCode(statusCode)
                .build();
    }

    public static ApiErrorResponse of(String message, int statusCode, Map<String, Object> details) {
        return ApiErrorResponse.builder()
                .message(message)
                .statusCode(statusCode)
                .details(details)
                .build();
    }
}
