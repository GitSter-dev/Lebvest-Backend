package com.application.lebvest.models.exceptions;

import com.application.lebvest.models.dtos.ApiErrorDto;
import com.application.lebvest.models.dtos.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseDto<Object> handleInvalidArgumentException(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            String verdict = fe.getDefaultMessage() ;
            details.merge(fe.getField(), verdict, (a, b) -> a + "; " + b);
        }
        for (ObjectError oe : ex.getBindingResult().getGlobalErrors()) {
            String verdict = oe.getDefaultMessage();
            details.merge(oe.getObjectName(), verdict, (a, b) -> a + "; " + b);
        }
        ApiErrorDto error = ApiErrorDto.builder()
                .message("Validation failed")
                .details(details)
                .build();
        return ApiResponseDto.error(error, HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(ResourceConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponseDto<Object> handleResourceConflictException(ResourceConflictException ex) {
        ApiErrorDto error = ApiErrorDto.builder()
                .message(ex.getMessage())
                .build();
        return ApiResponseDto.error(error, HttpStatus.CONFLICT.value());
    }


}
