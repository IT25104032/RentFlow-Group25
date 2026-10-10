package com.compulin.rentflow.exception.module3;

// Thrown when an invoice can't be generated because rental information is missing
public class InvoiceGenerationException extends RuntimeException {
    public InvoiceGenerationException(String message) {
        super(message);
    }
}

