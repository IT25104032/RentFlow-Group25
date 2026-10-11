package com.compulin.rentflow.entity.module4;

import jakarta.persistence.*;

import java.time.LocalDateTime;


/*
 * A return item which is one item(with an equipment_id) that is returned.
 */
@Entity
@Table(name = "return_item")
public class ReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_item_id")
    private Integer returnItemId;

    @ManyToOne
    @JoinColumn(name = "return_id", nullable = false)
    private RentalReturn rentalReturn;

    @Column(name = "rental_item_id", nullable = false)
    private Integer rentalItemId;

    @Column(name = "qty_returned", nullable = false)
    private Integer quantityReturned;

    @Column(name = "condition_status", nullable = false)
    private String conditionStatus;

    @Column(name = "inspection_notes")
    private String inspectionNotes;

    @Column(name = "returned_at", nullable = false)
    private LocalDateTime returnedAt;

    public ReturnItem() {
    }

    public Integer getReturnItemId() {
        return returnItemId;
    }

    public void setReturnItemId(Integer returnItemId) {
        this.returnItemId = returnItemId;
    }

    public RentalReturn getRentalReturn() {
        return rentalReturn;
    }

    public void setRentalReturn(RentalReturn rentalReturn) {
        this.rentalReturn = rentalReturn;
    }

    public Integer getRentalItemId() {
        return rentalItemId;
    }

    public void setRentalItemId(Integer rentalItemId) {
        this.rentalItemId = rentalItemId;
    }

    public Integer getQuantityReturned() {
        return quantityReturned;
    }

    public void setQuantityReturned(Integer quantityReturned) {
        this.quantityReturned = quantityReturned;
    }

    public String getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(String conditionStatus) {
        this.conditionStatus = conditionStatus;
    }

    public String getInspectionNotes() {
        return inspectionNotes;
    }

    public void setInspectionNotes(String inspectionNotes) {
        this.inspectionNotes = inspectionNotes;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }
}
