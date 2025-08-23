package com.furkancavdar.qrmenu.common.exception;

import com.furkancavdar.qrmenu.auth.application.exception.SessionOwnerException;
import com.furkancavdar.qrmenu.common.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SessionOwnerException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleSessionOwnerException(SessionOwnerException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ApiResponse.error("Validation failed", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        return ApiResponse.error("Validation failed: " + message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<String> handleAccessDeniedException(AccessDeniedException ex) {
        return ApiResponse.error("Access denied: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error: " + ex.getMessage()));
    }

    @ExceptionHandler(NotImplementedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handeNotImplementedException(NotImplementedException ex) {
        return ApiResponse.error("Not implemented");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ApiResponse.error("Message not readable", e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return ApiResponse.error(ex.getMessage());
    }
}
