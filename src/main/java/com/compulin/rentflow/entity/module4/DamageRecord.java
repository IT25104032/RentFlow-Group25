package com.compulin.rentflow.entity.module4;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * A damage record: units of a rental line that's damaged.
 */@Entity
@Table(name = "damage_record")
public class DamageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "damage_id")
    private Integer damageId;

    @ManyToOne
    @JoinColumn(name = "return_item_id", nullable = false)
    private ReturnItem returnItem;

    @Column(name = "damaged_quantity", nullable = false)
    private Integer damagedQuantity;

    @Column(name = "damage_description", nullable = false, length = 500)
    private String damageDescription;

    @Column(name = "damage_level", nullable = false, length = 20)
    private String damageLevel;

    @Column(name = "estimated_cost", precision = 12, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "final_charge", precision = 12, scale = 2)
    private BigDecimal finalCharge;

    @Column(name = "assessed_by", nullable = false)
    private Integer assessedBy;

    @Column(name = "assessment_date", nullable = false)
    private LocalDateTime assessmentDate;

    @Column(name = "damage_status", nullable = false, length = 30)
    private String status;

    public DamageRecord() {
    }

    public Integer getDamageId() {
        return damageId;
    }

    public void setDamageId(Integer damageId) {
        this.damageId = damageId;
    }

    public ReturnItem getReturnItem() {
        return returnItem;
    }

    public void setReturnItem(ReturnItem returnItem) {
        this.returnItem = returnItem;
    }

    public Integer getDamagedQuantity() {
        return damagedQuantity;
    }

    public void setDamagedQuantity(Integer damagedQuantity) {
        this.damagedQuantity = damagedQuantity;
    }

    public String getDamageDescription() {
        return damageDescription;
    }

    public void setDamageDescription(String damageDescription) {
        this.damageDescription = damageDescription;
    }

    public String getDamageLevel() {
        return damageLevel;
    }

    public void setDamageLevel(String damageLevel) {
        this.damageLevel = damageLevel;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public BigDecimal getFinalCharge() {
        return finalCharge;
    }

    public void setFinalCharge(BigDecimal finalCharge) {
        this.finalCharge = finalCharge;
    }

    public Integer getAssessedBy() {
        return assessedBy;
    }

    public void setAssessedBy(Integer assessedBy) {
        this.assessedBy = assessedBy;
    }

    public LocalDateTime getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(LocalDateTime assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
