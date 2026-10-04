package com.compulin.rentflow.dto.module4;

public class RentalSearchResultDTO {

    private Integer rentalId;
    private Integer customerId;
    private String customerName;
    private String startDate;
    private String dueDate;
    private String rentalStatus;

    public RentalSearchResultDTO() {
    }

    public RentalSearchResultDTO(
            Integer rentalId,
            Integer customerId,
            String customerName,
            String startDate,
            String dueDate,
            String rentalStatus) {

        this.rentalId = rentalId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.rentalStatus = rentalStatus;
    }

    public Integer getRentalId() {
        return rentalId;
    }

    public void setRentalId(Integer rentalId) {
        this.rentalId = rentalId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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
}