package com.application.lebvest.models.exceptions;

import com.application.lebvest.models.dtos.ApiErrorDto;
import com.application.lebvest.models.dtos.ApiResponseDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(
                ApiResponseDto.error(404,
                        ApiErrorDto.builder()
                                .message(ex.getMessage())
                                .details(Map.ofEntries(
                                        Map.entry("Resource", ex.getResourceClass().getSimpleName()),
                                        Map.entry("Field", ex.getResourceField()),
                                        Map.entry("Value", ex.getValue().toString())
                                ))
                                .build())
        );
    }

    @ExceptionHandler(MailMessagingException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMailMessagingException(MailMessagingException ex) {
        return ResponseEntity.status(500).body(
                ApiResponseDto.error(500,
                        ApiErrorDto.builder()
                                .message(ex.getMessage())
                                .details(Map.ofEntries(
                                        Map.entry("recipient", ex.getRecipient())
                                ))
                                .build())
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponseDto.error(409,
                        ApiErrorDto.builder()
                                .message("Data integrity violation")
                                .details(Map.of("cause", ex.getMostSpecificCause().getMessage()))
                                .build())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        return ResponseEntity.status(400).body(
                ApiResponseDto.error(400,
                        ApiErrorDto.builder()
                                .message("Validation failed")
                                .details(details)
                                .build())
        );
    }
}