package com.application.lebvest.models.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ApiResponseDto<T>(
        Instant timestamp,
        T data,
        @NotNull
        Integer statusCode,
        ApiErrorDto error
) {

        public static <T> ApiResponseDto<T> ok(T data, int statusCode) {
                return ApiResponseDto.<T>builder()
                        .timestamp(Instant.now())
                        .data(data)
                        .statusCode(statusCode)
                        .error(null)
                        .build();
        }

        public static <T> ApiResponseDto<T> error(int statusCode, ApiErrorDto error) {
                return ApiResponseDto.<T>builder()
                        .timestamp(Instant.now())
                        .data(null)
                        .statusCode(statusCode)
                        .error(error)
                        .build();
        }
}