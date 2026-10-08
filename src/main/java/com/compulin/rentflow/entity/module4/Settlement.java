package com.compulin.rentflow.entity.module4;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * MODULE 4 - final settlement of a rental (one row per rental).
 * settlement_status: PENDING (rentee still owes money) or SETTLED.
 */
@Entity
@Table(name = "settlement")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Integer settlementId;

    @Column(name = "rental_id", nullable = false, unique = true)
    private Integer rentalId;

    @Column(name = "rental_charges", precision = 12, scale = 2)
    private BigDecimal rentalCharges;

    @Column(name = "late_charges", precision = 12, scale = 2)
    private BigDecimal lateCharges;

    @Column(name = "damage_charges", precision = 12, scale = 2)
    private BigDecimal damageCharges;

    @Column(name = "lost_item_charges", precision = 12, scale = 2)
    private BigDecimal lostItemCharges;

    @Column(name = "total_charges", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCharges;

    @Column(name = "deposit_used", precision = 12, scale = 2)
    private BigDecimal depositUsed;

    @Column(name = "deposit_refunded", precision = 12, scale = 2)
    private BigDecimal depositRefunded;

    @Column(name = "final_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalBalance;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "settled_by", nullable = false)
    private Integer settledBy;

    @Column(name = "settlement_status", nullable = false, length = 20)
    private String settlementStatus;

    public Settlement() {
    }

    public Integer getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(Integer settlementId) {
        this.settlementId = settlementId;
    }

    public Integer getRentalId() {
        return rentalId;
    }

    public void setRentalId(Integer rentalId) {
        this.rentalId = rentalId;
    }

    public BigDecimal getRentalCharges() {
        return rentalCharges;
    }

    public void setRentalCharges(BigDecimal rentalCharges) {
        this.rentalCharges = rentalCharges;
    }

    public BigDecimal getLateCharges() {
        return lateCharges;
    }

    public void setLateCharges(BigDecimal lateCharges) {
        this.lateCharges = lateCharges;
    }

    public BigDecimal getDamageCharges() {
        return damageCharges;
    }

    public void setDamageCharges(BigDecimal damageCharges) {
        this.damageCharges = damageCharges;
    }

    public BigDecimal getLostItemCharges() {
        return lostItemCharges;
    }

    public void setLostItemCharges(BigDecimal lostItemCharges) {
        this.lostItemCharges = lostItemCharges;
    }

    public BigDecimal getTotalCharges() {
        return totalCharges;
    }

    public void setTotalCharges(BigDecimal totalCharges) {
        this.totalCharges = totalCharges;
    }

    public BigDecimal getDepositUsed() {
        return depositUsed;
    }

    public void setDepositUsed(BigDecimal depositUsed) {
        this.depositUsed = depositUsed;
    }

    public BigDecimal getDepositRefunded() {
        return depositRefunded;
    }

    public void setDepositRefunded(BigDecimal depositRefunded) {
        this.depositRefunded = depositRefunded;
    }

    public BigDecimal getFinalBalance() {
        return finalBalance;
    }

    public void setFinalBalance(BigDecimal finalBalance) {
        this.finalBalance = finalBalance;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(LocalDateTime settledAt) {
        this.settledAt = settledAt;
    }

    public Integer getSettledBy() {
        return settledBy;
    }

    public void setSettledBy(Integer settledBy) {
        this.settledBy = settledBy;
    }

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
    }
}
