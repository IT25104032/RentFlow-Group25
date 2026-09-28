package com.compulin.rentflow.dto.module2;


import java.util.List;

public class RentalConfirmationRequest {

    private RentalRequest rental;

    private List<RentalItemRequest> rentalItems;


    public RentalRequest getRental() {
        return rental;
    }

    public void setRental(RentalRequest rental) {
        this.rental = rental;
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