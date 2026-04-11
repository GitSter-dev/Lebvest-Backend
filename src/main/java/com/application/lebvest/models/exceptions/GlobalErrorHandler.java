package com.application.lebvest.models.exceptions;

import com.application.lebvest.models.dtos.ApiErrorDto;
import com.application.lebvest.models.dtos.ApiResponseDto;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.Map;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleResourceConflictException(
            ResourceConflictException ex
    ) {
        return ResponseEntity
                .status(409)
                .body(
                        ApiResponseDto.error(
                                409,
                                ApiErrorDto
                                        .builder()
                                        .message(ex.getMessage())
                                        .build()
                        )
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleMethodArgumentsNotValidException(
            MethodArgumentNotValidException ex
    ) {
        BindingResult result = ex.getBindingResult();

        Map<String, Object> errors = result.getFieldErrors()
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (existing, replacement) -> existing // in case of duplicate fields
                ));

        return ResponseEntity
                .badRequest()
                .body(
                        ApiResponseDto.error(
                                400,
                                ApiErrorDto.builder()
                                        .message("Validation failed")
                                        .details(errors)
                                        .build()
                        )
                );
    }
}
