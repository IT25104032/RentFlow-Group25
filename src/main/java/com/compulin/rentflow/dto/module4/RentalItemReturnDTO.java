package com.compulin.rentflow.dto.module4;

public class RentalItemReturnDTO {

    private Integer rentalItemId;
    private Integer equipmentId;
    private String equipmentName;

    private Integer issuedQuantity;
    private Integer alreadyReturnedQuantity;
    private Integer remainingQuantity;

    private String itemStatus;

    public RentalItemReturnDTO() {
    }

    public Integer getRentalItemId() {
        return rentalItemId;
    }

    public void setRentalItemId(Integer rentalItemId) {
        this.rentalItemId = rentalItemId;
    }

    public Integer getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Integer equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public Integer getIssuedQuantity() {
        return issuedQuantity;
    }

    public void setIssuedQuantity(Integer issuedQuantity) {
        this.issuedQuantity = issuedQuantity;
    }

    public Integer getAlreadyReturnedQuantity() {
        return alreadyReturnedQuantity;
    }

    public void setAlreadyReturnedQuantity(Integer alreadyReturnedQuantity) {
        this.alreadyReturnedQuantity = alreadyReturnedQuantity;
    }

    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(Integer remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }
}