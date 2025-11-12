package com.foodapp.common;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(
                ex.getStatus(),
                "API_ERROR",
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(
                500,
                "INTERNAL_SERVER_ERROR",
                ex.getMessage()==null ? "Unexpected error" : ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(500).body(body);
    }
}
