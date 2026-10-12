package com.compulin.rentflow.dto.module3;

import com.compulin.rentflow.entity.module3.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// One row of an invoice's payment history
public record PaymentHistoryItem(
        Integer paymentId,
        Integer invoiceId,
        BigDecimal amount,
        LocalDateTime paymentDate,
        String paymentMethod,
        String referenceNo,
        Integer receivedBy,
        String paymentStatus
) {
    public static PaymentHistoryItem from(Payment p) {
        return new PaymentHistoryItem(
                p.getPaymentId(),
                p.getInvoice().getInvoiceId(),
                p.getAmount(),
                p.getPaymentDate(),
                p.getPaymentMethod().name(),
                p.getReferenceNo(),
                p.getReceivedBy(),
                p.getPaymentStatus().name()
        );
    }
}
