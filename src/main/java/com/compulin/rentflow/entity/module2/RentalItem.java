package com.compulin.rentflow.entity.module2;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "rental_item")
public class RentalItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rental_item_id")
    private Integer rentalItemId;


    @Column(name = "rental_id", nullable = false)
    private Integer rentalId;


    @Column(name = "equipment_id", nullable = false)
    private Integer equipmentId;


    @Column(name = "quantity", nullable = false)
    private Integer quantity;


    @Column(
            name = "rate_per_unit",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal ratePerUnit;


    @Column(
            name = "rate_period",
            nullable = false,
            length = 20
    )
    private String ratePeriod;


    @Column(
            name = "deposit_per_unit",
            precision = 12,
            scale = 2
    )
    private BigDecimal depositPerUnit;


    @Column(
            name = "line_deposit",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal lineDeposit;


    @Column(
            name = "item_status",
            nullable = false,
            length = 30
    )
    private String itemStatus;


    @Column(name = "issued_at")
    private LocalDateTime issuedAt;


    @PrePersist
    protected void onCreate() {

        if (itemStatus == null) {
            itemStatus = "SELECTED";
        }

        if (depositPerUnit == null) {
            depositPerUnit = BigDecimal.ZERO;
        }

        if (lineDeposit == null) {
            lineDeposit = BigDecimal.ZERO;
        }
    }


    public Integer getRentalItemId() {
        return rentalItemId;
    }

    public void setRentalItemId(Integer rentalItemId) {
        this.rentalItemId = rentalItemId;
    }


    public Integer getRentalId() {
        return rentalId;
    }

    public void setRentalId(Integer rentalId) {
        this.rentalId = rentalId;
    }


    public Integer getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Integer equipmentId) {
        this.equipmentId = equipmentId;
    }


    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }


    public BigDecimal getRatePerUnit() {
        return ratePerUnit;
    }

    public void setRatePerUnit(BigDecimal ratePerUnit) {
        this.ratePerUnit = ratePerUnit;
    }


    public String getRatePeriod() {
        return ratePeriod;
    }

    public void setRatePeriod(String ratePeriod) {
        this.ratePeriod = ratePeriod;
    }


    public BigDecimal getDepositPerUnit() {
        return depositPerUnit;
    }

    public void setDepositPerUnit(BigDecimal depositPerUnit) {
        this.depositPerUnit = depositPerUnit;
    }


    public BigDecimal getLineDeposit() {
        return lineDeposit;
    }

    public void setLineDeposit(BigDecimal lineDeposit) {
        this.lineDeposit = lineDeposit;
    }


    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }


    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}