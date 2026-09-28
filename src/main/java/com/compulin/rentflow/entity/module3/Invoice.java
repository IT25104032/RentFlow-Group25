package com.compulin.rentflow.entity.module3;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Integer invoiceId;

    @Column(name = "rental_id", nullable = false)
    private Integer rentalId;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "additional_charges", precision = 12, scale = 2)
    private BigDecimal additionalCharges = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "amount_paid", precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "balance_due", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceDue;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", nullable = false, length = 25)
    private InvoiceStatus invoiceStatus = InvoiceStatus.UNPAID;

    public enum InvoiceStatus {
        UNPAID, PARTIALLY_PAID, PAID, CANCELLED
    }

    public Invoice() {
    }

    public Invoice(Integer rentalId, LocalDate invoiceDate, LocalDate dueDate, BigDecimal subtotal,
                   BigDecimal additionalCharges, BigDecimal totalAmount, BigDecimal amountPaid,
                   BigDecimal balanceDue, InvoiceStatus invoiceStatus) {
        this.rentalId = rentalId;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.subtotal = subtotal;
        this.additionalCharges = additionalCharges != null ? additionalCharges : BigDecimal.ZERO;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid != null ? amountPaid : BigDecimal.ZERO;
        this.balanceDue = balanceDue;
        this.invoiceStatus = invoiceStatus;
    }

    // Getters and Setters
    public Integer getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {

        this.invoiceId = invoiceId;
    }

    public Integer getRentalId() {

        return rentalId;
    }

    public void setRentalId(Integer rentalId) {

        this.rentalId = rentalId;
    }

    public LocalDate getInvoiceDate() {

        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {

        this.invoiceDate = invoiceDate;
    }

    public LocalDate getDueDate() {

        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {

        this.dueDate = dueDate;
    }

    public BigDecimal getSubtotal() {

        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {

        this.subtotal = subtotal;
    }

    public BigDecimal getAdditionalCharges() {

        return additionalCharges;
    }

    public void setAdditionalCharges(BigDecimal additionalCharges) {

        this.additionalCharges = additionalCharges;
    }

    public BigDecimal getTotalAmount() {

        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {

        this.totalAmount = totalAmount;
    }

    public BigDecimal getAmountPaid() {

        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {

        this.amountPaid = amountPaid;
    }

    public BigDecimal getBalanceDue() {

        return balanceDue;
    }

    public void setBalanceDue(BigDecimal balanceDue) {

        this.balanceDue = balanceDue;
    }

    public InvoiceStatus getInvoiceStatus() {

        return invoiceStatus;
    }

    public void setInvoiceStatus(InvoiceStatus invoiceStatus) {

        this.invoiceStatus = invoiceStatus;
    }
}