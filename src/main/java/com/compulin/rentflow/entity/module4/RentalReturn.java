package com.compulin.rentflow.entity.module4;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rental_return")
public class RentalReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_id")
    private Integer returnId;

    @Column(name = "rental_id", nullable = false)
    private Integer rentalId;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Column(name = "processed_by", nullable = false)
    private Integer processedBy;

    @Column(name = "return_type", nullable = false)
    private String returnType;

    @Column(name = "notes")
    private String notes;

    public RentalReturn() {
    }

    public Integer getReturnId() {
        return returnId;
    }

    public void setReturnId(Integer returnId) {
        this.returnId = returnId;
    }

    public Integer getRentalId() {
        return rentalId;
    }

    public void setRentalId(Integer rentalId) {
        this.rentalId = rentalId;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public Integer getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Integer processedBy) {
        this.processedBy = processedBy;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
