package com.compulin.rentflow.dto.module4;

import java.util.List;

public class RentalReturnDetailsDTO {

    private Integer rentalId;
    private String customerName;
    private String customerPhone;
    private String startDate;
    private String dueDate;
    private String rentalStatus;

    private List<RentalItemReturnDTO> items;

    public RentalReturnDetailsDTO() {
    }

    public Integer getRentalId() {
        return rentalId;
    }

    public void setRentalId(Integer rentalId) {
        this.rentalId = rentalId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getRentalStatus() {
        return rentalStatus;
    }

    public void setRentalStatus(String rentalStatus) {
        this.rentalStatus = rentalStatus;
    }

    public List<RentalItemReturnDTO> getItems() {
        return items;
    }

    public void setItems(List<RentalItemReturnDTO> items) {
        this.items = items;
    }
}