package com.compulin.rentflow.controller.module2;


import com.compulin.rentflow.dto.module2.RentalConfirmationRequest;
import com.compulin.rentflow.dto.module2.RentalConfirmationResponse;
import com.compulin.rentflow.service.module2.RentalConfirmationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rentals")
public class RentalConfirmationController {

    private final RentalConfirmationService rentalConfirmationService;

    public RentalConfirmationController(
            RentalConfirmationService rentalConfirmationService
    ) {
        this.rentalConfirmationService =
                rentalConfirmationService;
    }

    @PostMapping("/confirm")
    public ResponseEntity<RentalConfirmationResponse> confirmRental(
            @RequestBody RentalConfirmationRequest request
    ) {

        RentalConfirmationResponse response =
                rentalConfirmationService.confirmRental(
                        request
                );

        return ResponseEntity.ok(response);
    }
}