package com.compulin.rentflow.entity.module2;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rental_extension")
public class RentalExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "extension_id")
    private Integer extensionId;

    @Column(name = "rental_id", nullable = false)
    private Integer rentalId;

    @Column(name = "old_due_date", nullable = false)
    private LocalDate oldDueDate;

    @Column(name = "new_due_date", nullable = false)
    private LocalDate newDueDate;

    @Column(name = "extension_charge", precision = 12, scale = 2)
    private BigDecimal extensionCharge;

    @Column(name = "approved_by", nullable = false)
    private Integer approvedBy;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (extensionCharge == null) {
            extensionCharge = BigDecimal.ZERO;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

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