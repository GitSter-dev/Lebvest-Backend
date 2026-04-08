package com.application.lebvest.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;


@Builder
public record ApiResponseDto<T>(
        @NotBlank
        Integer statusCode,

        T data,

        @NotNull
        Instant timestamp,
        ApiErrorDto error
) {

    public static <T> ApiResponseDto<T> ok(
            Integer statusCode,
            T data
    ) {
        return ApiResponseDto
                .<T>builder()
                .data(data)
                .timestamp(Instant.now())
                .statusCode(statusCode)
                .build();
    }

    public static ApiResponseDto<Object> error(
            Integer statusCode,
            ApiErrorDto error
    ) {
        return ApiResponseDto
                .builder()
                .statusCode(statusCode)
                .error(error)
                .build();
    }
}
