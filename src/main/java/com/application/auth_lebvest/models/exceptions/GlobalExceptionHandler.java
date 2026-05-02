package com.application.auth_lebvest.models.exceptions;

import com.application.auth_lebvest.models.dtos.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exceptions.ResourceConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceConflict(Exceptions.ResourceConflictException ex) {
        ApiErrorResponse response = ApiErrorResponse.of(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                Map.of("field", ex.getField(), "value", ex.getValue())
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> violations = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        f -> f.getField(),
                        f -> f.getDefaultMessage(),
                        (existing, duplicate) -> existing
                ));

        ApiErrorResponse response = ApiErrorResponse.of(
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                violations
        );
        return ResponseEntity.badRequest().body(response);
    }
}
