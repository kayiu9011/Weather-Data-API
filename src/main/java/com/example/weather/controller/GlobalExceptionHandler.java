package com.example.weather.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import com.example.weather.dto.ApiError;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(
            ResponseStatusException exception,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return buildResponse(status, exception.getReason(), request.getRequestURI());
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiError> handleInvalidParameter(
            Exception exception,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "A required query parameter is missing or invalid",
                request.getRequestURI());
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiError> handleWeatherServiceFailure(
            RestClientException exception,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                "The external weather service is currently unavailable",
                request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedError(
            Exception exception,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request.getRequestURI());
    }

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            String message,
            String path) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path);
        return ResponseEntity.status(status).body(error);
    }
}
