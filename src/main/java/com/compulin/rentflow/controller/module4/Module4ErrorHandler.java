package com.compulin.rentflow.controller.module4;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/*
 * MODULE 4 - turns validation errors from the Module 4 services
 * (e.g. "Only 3 unit(s) are still out") into HTTP 400 { "message": "..." }
 * so the React pages can show the reason instead of an empty 500.
 * Only applies to the Module 4 controllers.
 */
@RestControllerAdvice(basePackages = "com.compulin.rentflow.controller.module4")
public class Module4ErrorHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badInput(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> databaseRule(DataIntegrityViolationException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", "The database rejected this change: " + ex.getMostSpecificCause().getMessage()));
    }
}
