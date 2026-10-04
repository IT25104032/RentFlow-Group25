package com.compulin.rentflow.dto.module4;

import java.util.List;

public class ProcessReturnRequest {

    private Integer rentalId;
    private Integer processedBy;
    private String notes;
    private List<ReturnItemRequest> items;

    public ProcessReturnRequest() {
    }

    public Integer getRentalId() {
        return rentalId;
    }

    public void setRentalId(Integer rentalId) {
        this.rentalId = rentalId;
    }

    public Integer getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Integer processedBy) {
        this.processedBy = processedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<ReturnItemRequest> getItems() {
        return items;
    }

    public void setItems(List<ReturnItemRequest> items) {
        this.items = items;
    }
}