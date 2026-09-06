package com.salarymanagement.exception;

import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<?> handle(ApiException e, HttpServletRequest r) {
        return ResponseEntity.status(e instanceof NotFoundException ? 404 : 400)
                .body(java.util.Map.of("status", e instanceof NotFoundException ? 404 : 400, "code", e.getCode(),
                        "message", e.getMessage(), "path", r.getRequestURI()));
    }
}
