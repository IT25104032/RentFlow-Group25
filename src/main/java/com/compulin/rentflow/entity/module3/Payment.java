package com.compulin.rentflow.entity.module3;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    @Column(name = "received_by", nullable = false)
    private Integer receivedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.COMPLETED;

    public enum PaymentMethod {
        CASH, CARD, BANK_TRANSFER
    }

    public enum PaymentStatus {
        COMPLETED, VOIDED
    }

    public Payment() {
    }

    public Payment(Invoice invoice, BigDecimal amount, LocalDateTime paymentDate,
                   PaymentMethod paymentMethod, String referenceNo, Integer receivedBy,
                   PaymentStatus paymentStatus) {
        this.invoice = invoice;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.referenceNo = referenceNo;
        this.receivedBy = receivedBy;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.COMPLETED;
    }

    // Getters and Setters
    public Integer getPaymentId() {

        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {

        this.paymentId = paymentId;
    }

    public Invoice getInvoice() {

        return invoice;
    }

    public void setInvoice(Invoice invoice) {

        this.invoice = invoice;
    }

    public BigDecimal getAmount() {

        return amount;
    }

    public void setAmount(BigDecimal amount) {

        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {

        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {

        this.paymentDate = paymentDate;
    }

    public PaymentMethod getPaymentMethod() {

        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {

        this.paymentMethod = paymentMethod;
    }

    public String getReferenceNo() {

        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {

        this.referenceNo = referenceNo;
    }

    public Integer getReceivedBy() {

        return receivedBy;
    }

    public void setReceivedBy(Integer receivedBy) {

        this.receivedBy = receivedBy;
    }

    public PaymentStatus getPaymentStatus() {

        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {

        this.paymentStatus = paymentStatus;
    }
}
