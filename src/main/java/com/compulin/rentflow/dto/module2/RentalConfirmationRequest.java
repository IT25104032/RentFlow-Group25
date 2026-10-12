package com.compulin.rentflow.dto.module2;


import java.util.List;
import java.math.BigDecimal;

public class RentalConfirmationRequest {

    private RentalRequest rental;

    private List<RentalItemRequest> rentalItems;

    // Final agreed security deposit (edited or calculated).
    private BigDecimal totalDeposit;


    public RentalRequest getRental() {
        return rental;
    }

    public void setRental(RentalRequest rental) {
        this.rental = rental;
    }


    public BigDecimal getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(BigDecimal totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public List<RentalItemRequest> getRentalItems() {
        return rentalItems;
    }

    public void setRentalItems(
            List<RentalItemRequest> rentalItems
    ) {
        this.rentalItems = rentalItems;
    }
}