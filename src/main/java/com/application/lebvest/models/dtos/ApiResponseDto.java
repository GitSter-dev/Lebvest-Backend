package com.application.lebvest.models.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ApiResponseDto<T>(
        @NotNull
        Integer statusCode,

        T data,

        Instant timestamp,

        ApiErrorDto error

) {

        public static <T> ApiResponseDto<T> ok(T data,
                                               Integer statusCode) {
                return ApiResponseDto.<T>builder()
                        .statusCode(statusCode)
                        .data(data)
                        .timestamp(Instant.now())
                        .build();
        }

        public static <T> ApiResponseDto<Object> error(ApiErrorDto error,
                                                     Integer statusCode) {
                return ApiResponseDto.builder()
                        .statusCode(statusCode)
                        .error(error)
                        .timestamp(Instant.now())
                        .build();
        }
}
