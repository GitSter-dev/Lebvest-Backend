package com.application.lebvest.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

    @NotNull
    private Integer statusCode;

    private T data;

    @NotNull
    private Instant timestamp;

    private ApiErrorDto error;

    public static <T> ApiResponseDto<T> ok(Integer statusCode, T data) {
        return ApiResponseDto.<T>builder()
                .statusCode(statusCode)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponseDto<T> error(Integer statusCode, ApiErrorDto error) {
        return ApiResponseDto.<T>builder()
                .statusCode(statusCode)
                .error(error)
                .timestamp(Instant.now())
                .build();
    }
}