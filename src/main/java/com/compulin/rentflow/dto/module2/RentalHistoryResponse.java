package com.compulin.rentflow.dto.module2;

import java.util.List;

public class RentalHistoryResponse {

    private RentalResponse rental;

    private List<RentalItemResponse> rentalItems;

    public RentalHistoryResponse() {
    }

    public RentalResponse getRental() {
        return rental;
    }

    public void setRental(RentalResponse rental) {
        this.rental = rental;
    }

    public List<RentalItemResponse> getRentalItems() {
        return rentalItems;
    }

    public void setRentalItems(List<RentalItemResponse> rentalItems) {
        this.rentalItems = rentalItems;
    }
}