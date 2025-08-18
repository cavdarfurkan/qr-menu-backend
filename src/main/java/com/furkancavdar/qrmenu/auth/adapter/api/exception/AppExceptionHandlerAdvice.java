package com.furkancavdar.qrmenu.auth.adapter.api.exception;

import com.furkancavdar.qrmenu.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppExceptionHandlerAdvice {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<String>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication failed: " + ex.getMessage()));
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ApiResponse<String>> handleInsufficientAuthenticationException(
            InsufficientAuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required: " + ex.getMessage()));
    }

    @RestController
    public static class CustomErrorController implements ErrorController {
        
        @RequestMapping("/error")
        public ResponseEntity<ApiResponse<String>> handleError(HttpServletRequest request) {
            Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
            Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
            String message = exception != null ? exception.getMessage() : "Unknown error";
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Resource not found: " + request.getRequestURI()));
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Access denied: " + message));
            } else if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Authentication required"));
            }
            
            return ResponseEntity
                    .status(statusCode != null ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(ApiResponse.error("Error: " + message));
        }
    }
} 