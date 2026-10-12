package com.compulin.rentflow.dto.module2;

import java.math.BigDecimal;


public class RentalItemRequest {

    private Integer rentalId;

    private Integer equipmentId;

    private Integer quantity;

    private BigDecimal ratePerUnit;

    private String ratePeriod;

    private BigDecimal depositPerUnit;

    private BigDecimal lineDeposit;

    private String itemStatus;


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
}