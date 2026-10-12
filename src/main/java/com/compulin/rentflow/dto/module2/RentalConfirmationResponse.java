package com.compulin.rentflow.dto.module2;


import java.util.List;

public class RentalConfirmationResponse {

    private RentalResponse rental;

    private List<RentalItemResponse> rentalItems;

    private String message;


    public RentalResponse getRental() {
        return rental;
    }

    public void setRental(
            RentalResponse rental
    ) {
        this.rental = rental;
    }


    public List<RentalItemResponse> getRentalItems() {
        return rentalItems;
    }

    public void setRentalItems(
            List<RentalItemResponse> rentalItems
    ) {
        this.rentalItems = rentalItems;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(
            String message
    ) {
        this.message = message;
    }
}