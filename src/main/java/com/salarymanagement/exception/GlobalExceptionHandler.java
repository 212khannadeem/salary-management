package com.salarymanagement.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handle(ApiException e, HttpServletRequest r) {
        HttpStatus status = e instanceof NotFoundException ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        if (e instanceof ConflictException) {
            status = HttpStatus.CONFLICT;
        }
        return buildResponse(status, e.getCode(), e.getMessage(), r.getRequestURI(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handle(MethodArgumentNotValidException e, HttpServletRequest r) {
        List<String> details = new ArrayList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            details.add(fieldError.getField() + " " + fieldError.getDefaultMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed",
                r.getRequestURI(), details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handle(ConstraintViolationException e, HttpServletRequest r) {
        List<String> details = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed",
                r.getRequestURI(), details);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handle(IllegalArgumentException e, HttpServletRequest r) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", e.getMessage(), r.getRequestURI(), List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handle(MethodArgumentTypeMismatchException e, HttpServletRequest r) {
        String message = "Invalid value for parameter '" + e.getName() + "'";
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", message, r.getRequestURI(), List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handle(MissingServletRequestParameterException e, HttpServletRequest r) {
        String message = "Required parameter '" + e.getParameterName() + "' is missing";
        return buildResponse(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", message, r.getRequestURI(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handle(Exception e, HttpServletRequest r) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error",
                r.getRequestURI(), List.of());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String code, String message,
            String path, List<String> details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("code", code);
        body.put("message", message);
        body.put("path", path);
        if (!details.isEmpty()) {
            body.put("details", details);
        }
        return ResponseEntity.status(status).body(body);
    }
}
