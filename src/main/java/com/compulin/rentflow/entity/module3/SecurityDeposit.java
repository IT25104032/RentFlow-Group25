package com.compulin.rentflow.entity.module3;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_deposit")
public class SecurityDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deposit_id")
    private Integer depositId;

    @Column(name = "rental_id", unique = true)
    private Integer rentalId;

    @Column(name = "calculated_deposit", nullable = false, precision = 12, scale = 2)
    private BigDecimal calculatedDeposit;

    @Column(name = "deposit_amount_received", nullable = false, precision = 12, scale = 2)
    private BigDecimal depositAmountReceived;

    @Column(name = "amount_deducted", precision = 12, scale = 2)
    private BigDecimal amountDeducted = BigDecimal.ZERO;

    @Column(name = "amount_refunded", precision = 12, scale = 2)
    private BigDecimal amountRefunded = BigDecimal.ZERO;

    @Column(name = "received_date")
    private LocalDateTime receivedDate;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(name = "received_by")
    private Integer receivedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_status", nullable = false, length = 25)
    private DepositStatus depositStatus = DepositStatus.PENDING;

    public enum DepositStatus {
        PENDING, HELD, PARTIALLY_REFUNDED, REFUNDED, FORFEITED
    }

    public SecurityDeposit() {
    }

    public SecurityDeposit(Integer rentalId, BigDecimal calculatedDeposit, BigDecimal depositAmountReceived,
                           BigDecimal amountDeducted, BigDecimal amountRefunded, LocalDateTime receivedDate,
                           LocalDateTime refundDate, Integer receivedBy, DepositStatus depositStatus) {
        this.rentalId = rentalId;
        this.calculatedDeposit = calculatedDeposit;
        this.depositAmountReceived = depositAmountReceived;
        this.amountDeducted = amountDeducted != null ? amountDeducted : BigDecimal.ZERO;
        this.amountRefunded = amountRefunded != null ? amountRefunded : BigDecimal.ZERO;
        this.receivedDate = receivedDate;
        this.refundDate = refundDate;
        this.receivedBy = receivedBy;
        this.depositStatus = depositStatus != null ? depositStatus : DepositStatus.PENDING;
    }

    // Getters and Setters
    public Integer getDepositId() {

        return depositId;
    }

    public void setDepositId(Integer depositId) {

        this.depositId = depositId;
    }

    public Integer getRentalId() {

        return rentalId;
    }

    public void setRentalId(Integer rentalId) {

        this.rentalId = rentalId;
    }

    public BigDecimal getCalculatedDeposit() {

        return calculatedDeposit;
    }

    public void setCalculatedDeposit(BigDecimal calculatedDeposit) {

        this.calculatedDeposit = calculatedDeposit;
    }

    public BigDecimal getDepositAmountReceived() {

        return depositAmountReceived;
    }

    public void setDepositAmountReceived(BigDecimal depositAmountReceived) {

        this.depositAmountReceived = depositAmountReceived;
    }

    public BigDecimal getAmountDeducted() {

        return amountDeducted;
    }

    public void setAmountDeducted(BigDecimal amountDeducted) {

        this.amountDeducted = amountDeducted;
    }

    public BigDecimal getAmountRefunded() {

        return amountRefunded;
    }

    public void setAmountRefunded(BigDecimal amountRefunded) {

        this.amountRefunded = amountRefunded;
    }

    public LocalDateTime getReceivedDate() {

        return receivedDate;
    }

    public void setReceivedDate(LocalDateTime receivedDate) {

        this.receivedDate = receivedDate;
    }

    public LocalDateTime getRefundDate() {

        return refundDate;
    }

    public void setRefundDate(LocalDateTime refundDate) {

        this.refundDate = refundDate;
    }

    public Integer getReceivedBy() {

        return receivedBy;
    }

    public void setReceivedBy(Integer receivedBy) {

        this.receivedBy = receivedBy;
    }

    public DepositStatus getDepositStatus() {

        return depositStatus;
    }

    public void setDepositStatus(DepositStatus depositStatus) {

        this.depositStatus = depositStatus;
    }
}
