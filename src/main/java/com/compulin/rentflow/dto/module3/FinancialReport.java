package com.compulin.rentflow.dto.module3;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Result of a company-wide report
public record FinancialReport(
        String reportType,
        String title,
        LocalDate from,
        LocalDate to,
        List<InvoiceRow> invoices,
        List<PaymentRow> payments,
        List<MethodTotal> methodTotals,
        int recordCount,
        BigDecimal totalInvoiced,
        BigDecimal totalPaid,
        BigDecimal totalOutstanding
) {
    public record InvoiceRow(Integer invoiceId, Integer rentalId, String customerName,
                             LocalDate invoiceDate, LocalDate dueDate,
                             BigDecimal totalAmount, BigDecimal amountPaid, BigDecimal balanceDue,
                             String invoiceStatus, int daysOverdue) {}

    public record PaymentRow(Integer paymentId, Integer invoiceId, Integer rentalId, String customerName,
                             LocalDateTime paymentDate, String paymentMethod, String referenceNo,
                             BigDecimal amount, String paymentStatus, String receivedByName) {}

    public record MethodTotal(String paymentMethod, int count, BigDecimal total) {}
}

