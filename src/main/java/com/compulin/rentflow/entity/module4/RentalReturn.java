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

    //@ManyToOne
    @JoinColumn(name = "rental_id", nullable = false)
    private Integer rentalId;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    //@ManyToOne
    @JoinColumn(name = "processed_by", nullable = false)
    private Integer processedBy;

    @Column(name = "return_type", nullable = false, length = 20)
    private String returnType;

    @Column(length = 500)
    private String notes;

    //no-argument constructor to create entity objects
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

    public void setRental(Integer rentalId) {
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
