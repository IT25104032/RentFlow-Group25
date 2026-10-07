package com.compulin.rentflow.controller.module2;

import com.compulin.rentflow.dto.module2.RentalRequest;
import com.compulin.rentflow.dto.module2.RentalResponse;
import com.compulin.rentflow.dto.module2.RentalHistoryResponse;
import com.compulin.rentflow.service.module2.RentalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping
    public ResponseEntity<RentalResponse> createRental(
            @RequestBody RentalRequest request) {

        try {
            RentalResponse response =
                    rentalService.createRental(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{rentalId}")
    public ResponseEntity<RentalResponse> getRental(
            @PathVariable Integer rentalId,
            @RequestParam Integer companyId) {

        RentalResponse response =
                rentalService.getRental(rentalId, companyId);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<RentalHistoryResponse>> getCustomerRentalHistory(
            @PathVariable Integer customerId,
            @RequestParam Integer companyId) {

        List<RentalHistoryResponse> history =
                rentalService.getCustomerRentalHistory(
                        customerId,
                        companyId
                );

        return ResponseEntity.ok(history);
    }

    @GetMapping("/active")
    public ResponseEntity<List<RentalResponse>> getActiveRentals(
            @RequestParam Integer companyId) {

        return ResponseEntity.ok(
                rentalService.getActiveRentals(companyId)
        );
    }

    @GetMapping("/for-issue")
    public ResponseEntity<List<RentalResponse>> getRentalsForIssue(
            @RequestParam Integer companyId) {

        return ResponseEntity.ok(
                rentalService.getRentalsForIssue(companyId)
        );
    }

    @GetMapping("/for-extension")
    public ResponseEntity<List<RentalResponse>> getRentalsForExtension(
            @RequestParam Integer companyId) {

        return ResponseEntity.ok(
                rentalService.getRentalsForExtension(companyId)
        );
    }

    @PostMapping("/{rentalId}/issue")
    public ResponseEntity<RentalResponse> issueRental(
            @PathVariable Integer rentalId,
            @RequestParam Integer companyId) {

        try {
            return ResponseEntity.ok(
                    rentalService.issueRental(rentalId, companyId)
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{rentalId}/cancel")
    public ResponseEntity<RentalResponse> cancelRental(
            @PathVariable Integer rentalId,
            @RequestParam Integer companyId) {

        try {
            return ResponseEntity.ok(
                    rentalService.cancelRental(rentalId, companyId)
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<RentalResponse>> getOverdueRentals(
            @RequestParam Integer companyId) {

        return ResponseEntity.ok(
                rentalService.getOverdueRentals(companyId)
        );
    }

    @GetMapping("/due-soon")
    public ResponseEntity<List<RentalResponse>> getRentalsDueSoon(
            @RequestParam Integer companyId) {

        return ResponseEntity.ok(
                rentalService.getRentalsDueSoon(companyId)
        );
    }
}
