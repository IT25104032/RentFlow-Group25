package com.compulin.rentflow.dto.module2;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RentalExtensionResponse {

    private Integer extensionId;
    private Integer rentalId;
    private LocalDate oldDueDate;
    private LocalDate newDueDate;
    private BigDecimal extensionCharge;
    private Integer approvedBy;
    private String reason;
    private LocalDateTime createdAt;

    public Integer getExtensionId() {
        return extensionId;
    }

    public void setExtensionId(Integer extensionId) {
        this.extensionId = extensionId;
    }

    public Integer getRentalId() {
        return rentalId;
    }

    public void setRentalId(Integer rentalId) {
        this.rentalId = rentalId;
    }

    public LocalDate getOldDueDate() {
        return oldDueDate;
    }

    public void setOldDueDate(LocalDate oldDueDate) {
        this.oldDueDate = oldDueDate;
    }

    public LocalDate getNewDueDate() {
        return newDueDate;
    }

    public void setNewDueDate(LocalDate newDueDate) {
        this.newDueDate = newDueDate;
    }

    public BigDecimal getExtensionCharge() {
        return extensionCharge;
    }

    public void setExtensionCharge(BigDecimal extensionCharge) {
        this.extensionCharge = extensionCharge;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}