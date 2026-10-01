package com.compulin.rentflow.dto.module2;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RentalExtensionRequest {

    private Integer rentalId;
    private Integer companyId;
    private LocalDate newDueDate;
    private BigDecimal extensionCharge;
    private Integer approvedBy;
    private String reason;

    public Integer getRentalId() {
        return rentalId;
    }

    public void setRentalId(Integer rentalId) {
        this.rentalId = rentalId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
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
}