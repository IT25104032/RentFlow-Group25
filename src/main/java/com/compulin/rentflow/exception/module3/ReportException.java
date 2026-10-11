package com.compulin.rentflow.exception.module3;

// Thrown when report filters are invalid. Returned as 400.
public class ReportException extends RuntimeException {
    public ReportException(String message) {
        super(message);
    }
}

