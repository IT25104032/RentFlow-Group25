package com.compulin.rentflow.entity.module4;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * MODULE 4 - a lost note: units of a rental line that did not come back.
 * loss_type: LOST, STOLEN, NON_RETURNED
 * lost_status: PENDING (no charge yet), CHARGED, RECOVERED, SETTLED
 */
@Entity
@Table(name = "lost_item")
public class LostItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lost_item_id")
    private Integer lostItemId;

    @Column(name = "rental_item_id", nullable = false)
    private Integer rentalItemId;

    @Column(name = "quantity_lost", nullable = false)
    private Integer quantityLost;

    @Column(name = "loss_type", nullable = false, length = 30)
    private String lossType;

    @Column(name = "reported_date", nullable = false)
    private LocalDateTime reportedDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "replacement_cost_per_unit", precision = 12, scale = 2)
    private BigDecimal replacementCostPerUnit;

    @Column(name = "charge_amount", precision = 12, scale = 2)
    private BigDecimal chargeAmount;

    @Column(name = "reported_by", nullable = false)
    private Integer reportedBy;

    @Column(name = "lost_status", nullable = false, length = 30)
    private String lostStatus;

    @Column(name = "notes", length = 500)
    private String notes;

    public LostItem() {
    }

    public Integer getLostItemId() {
        return lostItemId;
    }

    public void setLostItemId(Integer lostItemId) {
        this.lostItemId = lostItemId;
    }

    public Integer getRentalItemId() {
        return rentalItemId;
    }

    public void setRentalItemId(Integer rentalItemId) {
        this.rentalItemId = rentalItemId;
    }

    public Integer getQuantityLost() {
        return quantityLost;
    }

    public void setQuantityLost(Integer quantityLost) {
        this.quantityLost = quantityLost;
    }

    public String getLossType() {
        return lossType;
    }

    public void setLossType(String lossType) {
        this.lossType = lossType;
    }

    public LocalDateTime getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(LocalDateTime reportedDate) {
        this.reportedDate = reportedDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getReplacementCostPerUnit() {
        return replacementCostPerUnit;
    }

    public void setReplacementCostPerUnit(BigDecimal replacementCostPerUnit) {
        this.replacementCostPerUnit = replacementCostPerUnit;
    }

    public BigDecimal getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(BigDecimal chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public Integer getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(Integer reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getLostStatus() {
        return lostStatus;
    }

    public void setLostStatus(String lostStatus) {
        this.lostStatus = lostStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
