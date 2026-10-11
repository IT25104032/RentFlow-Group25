package com.compulin.rentflow.dto.module3;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Financial position of one rental
public record RentalFinancialSummary(
        Integer rentalId,
        String customerName,
        String customerPhone,
        LocalDate startDate,
        LocalDate dueDate,
        String rentalStatus,
        Integer invoiceId,
        LocalDate invoiceDate,
        LocalDate invoiceDueDate,
        String invoiceStatus,
        BigDecimal rentalCharges,
        BigDecimal additionalCharges,
        BigDecimal depositDeduction,
        BigDecimal totalPayable,
        BigDecimal amountPaid,
        BigDecimal balanceDue,
        DepositInfo deposit,
        List<ChargeLine> charges,
        List<FinancialReport.PaymentRow> payments
) {
    public record DepositInfo(BigDecimal calculated, BigDecimal received, BigDecimal deducted,
                              BigDecimal refunded, String status) {}

    public record ChargeLine(String chargeType, String description, BigDecimal amount) {}
}

