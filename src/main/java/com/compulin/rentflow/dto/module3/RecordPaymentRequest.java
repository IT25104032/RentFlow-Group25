package com.compulin.rentflow.dto.module3;

import com.compulin.rentflow.entity.module3.Payment;
import java.math.BigDecimal;

public class RecordPaymentRequest {

    private Integer invoiceId;
    private BigDecimal amount;
    private Payment.PaymentMethod paymentMethod;
    private String referenceNo;

    // Getters and Setters
    public Integer getInvoiceId() {
        return invoiceId;
    }
    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Payment.PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(Payment.PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReferenceNo() {
        return referenceNo;
    }
    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }
}