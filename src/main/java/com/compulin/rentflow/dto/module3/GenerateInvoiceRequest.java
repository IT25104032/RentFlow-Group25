package com.compulin.rentflow.dto.module3;

import com.compulin.rentflow.entity.module3.Charge.ChargeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GenerateInvoiceRequest {

    private Integer rentalId;
    private LocalDate dueDate;
    private Integer createdBy;
    private List<ChargeItemDTO> charges;

    // --- Nested DTO for the itemized charges ---
    public static class ChargeItemDTO {
        private Integer rentalItemId;
        private ChargeType chargeType;
        private String description;
        private BigDecimal amount;

        // Getters and Setters for ChargeItemDTO
        public Integer getRentalItemId() {
            return rentalItemId;
        }
        public void setRentalItemId(Integer rentalItemId) {
            this.rentalItemId = rentalItemId;
        }
        public ChargeType getChargeType() {
            return chargeType;
        }
        public void setChargeType(ChargeType chargeType) {
            this.chargeType = chargeType;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public BigDecimal getAmount() {
            return amount;
        }
        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    //Getters and Setters
    public Integer getRentalId() {
        return rentalId;
    }
    public void setRentalId(Integer rentalId) {
        this.rentalId = rentalId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public List<ChargeItemDTO> getCharges() {
        return charges;
    }
    public void setCharges(List<ChargeItemDTO> charges) {
        this.charges = charges;
    }
}