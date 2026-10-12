package com.compulin.rentflow.controller.module2;


import com.compulin.rentflow.dto.module2.RentalItemRequest;
import com.compulin.rentflow.dto.module2.RentalItemResponse;
import com.compulin.rentflow.service.module2.RentalItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;


@RestController
@RequestMapping("/api/rental-items")
public class RentalItemController {

    private final RentalItemService rentalItemService;


    public RentalItemController(
            RentalItemService rentalItemService
    ) {
        this.rentalItemService =
                rentalItemService;
    }


    /*
     * Create a rental item.
     */
    @PostMapping
    public ResponseEntity<RentalItemResponse> createRentalItem(
            @RequestBody RentalItemRequest request
    ) {

        RentalItemResponse response =
                rentalItemService.createRentalItem(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    /*
     * Get all items belonging to a rental.
     */
    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<List<RentalItemResponse>> getRentalItems(
            @PathVariable Integer rentalId
    ) {

        List<RentalItemResponse> response =
                rentalItemService.getRentalItems(
                        rentalId
                );

        return ResponseEntity.ok(response);
    }


    /*
     * Get one rental item belonging to
     * a specific rental.
     */
    @GetMapping("/{rentalItemId}")
    public ResponseEntity<RentalItemResponse> getRentalItem(
            @PathVariable Integer rentalItemId,
            @RequestParam Integer rentalId
    ) {

        RentalItemResponse response =
                rentalItemService.getRentalItem(
                        rentalItemId,
                        rentalId
                );

        return ResponseEntity.ok(response);
    }

    /*
     * Issue a rental item.
     */
    @PostMapping("/{rentalItemId}/issue")
    public ResponseEntity<RentalItemResponse> issueRentalItem(
            @PathVariable Integer rentalItemId,
            @RequestParam Integer rentalId
    ) {

        try {

            RentalItemResponse response =
                    rentalItemService.issueRentalItem(
                            rentalItemId,
                            rentalId
                    );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException exception) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }
}