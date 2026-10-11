package com.compulin.rentflow.dto.module2;

import java.util.List;
import java.math.BigDecimal;

public class RentalHistoryResponse {

    private RentalResponse rental;

    private List<RentalItemResponse> rentalItems;

    // Final agreed security deposit parsed from the saved rental notes marker.
    private BigDecimal totalDeposit;

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

    public BigDecimal getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(BigDecimal totalDeposit) {
        this.totalDeposit = totalDeposit;
    }
}