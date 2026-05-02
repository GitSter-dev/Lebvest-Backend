package com.application.auth_lebvest.models.dtos;

import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ApiSuccessResponse<T> {
    private T data;
    private int statusCode;
    private String message;

    public static <T> ApiSuccessResponse<T> of(T data,
                                               int statusCode,
                                               String message) {
        return ApiSuccessResponse.<T>builder()
                .data(data)
                .statusCode(statusCode)
                .message(message)
                .build();
    }

    public static ApiSuccessResponse<Void> of(int statusCode, String message) {
        return ApiSuccessResponse.<Void>builder()
                .statusCode(statusCode)
                .message(message)
                .build();
    }


}
