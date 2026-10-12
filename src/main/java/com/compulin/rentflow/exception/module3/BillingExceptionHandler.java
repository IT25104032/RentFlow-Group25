package com.compulin.rentflow.exception.module3;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Map;

// Only applies to Module 3 controllers so other modules are not affected
@RestControllerAdvice(basePackages = "com.compulin.rentflow.controller.module3")
public class BillingExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateInvoiceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateInvoiceException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvoiceGenerationException.class)
    public ResponseEntity<Map<String, Object>> handleInvoiceGeneration(InvoiceGenerationException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ReportException.class)
    public ResponseEntity<Map<String, Object>> handleReport(ReportException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "Invalid value '" + ex.getValue() + "' for " + ex.getName() + ".");
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "message", message,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
