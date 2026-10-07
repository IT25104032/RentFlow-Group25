package com.compulin.rentflow.controller.module2;

import com.compulin.rentflow.dto.module2.RentalExtensionRequest;
import com.compulin.rentflow.dto.module2.RentalExtensionResponse;
import com.compulin.rentflow.service.module2.RentalExtensionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rental-extensions")
public class RentalExtensionController {

    private final RentalExtensionService rentalExtensionService;

    public RentalExtensionController(
            RentalExtensionService rentalExtensionService
    ) {
        this.rentalExtensionService = rentalExtensionService;
    }

    @PostMapping
    public ResponseEntity<RentalExtensionResponse> extendRental(
            @RequestBody RentalExtensionRequest request
    ) {
        try {
            RentalExtensionResponse response =
                    rentalExtensionService.extendRental(request);

            if (response == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<List<RentalExtensionResponse>> getExtensionHistory(
            @PathVariable Integer rentalId
    ) {
        List<RentalExtensionResponse> extensions =
                rentalExtensionService.getExtensionHistory(rentalId);

        return ResponseEntity.ok(extensions);
    }
}
