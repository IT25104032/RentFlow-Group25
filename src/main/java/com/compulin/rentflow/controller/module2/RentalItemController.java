package com.compulin.rentflow.controller.module2;

import com.compulin.rentflow.dto.module2.RentalItemRequest;
import com.compulin.rentflow.dto.module2.RentalItemResponse;
import com.compulin.rentflow.repository.module1.equipment_repo;
import com.compulin.rentflow.repository.module2.RentalRepository;
import com.compulin.rentflow.security.Module1SessionAccess;
import com.compulin.rentflow.service.module2.RentalItemService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/rental-items")
public class RentalItemController {

    private final RentalItemService rentalItemService;
    private final RentalRepository rentalRepository;
    private final equipment_repo equipmentRepository;

    public RentalItemController(
            RentalItemService rentalItemService,
            RentalRepository rentalRepository,
            equipment_repo equipmentRepository
    ) {
        this.rentalItemService = rentalItemService;
        this.rentalRepository = rentalRepository;
        this.equipmentRepository = equipmentRepository;
    }

    private Integer authenticatedCompanyId(HttpSession session) {
        Integer companyId = Module1SessionAccess.getCompanyId(session);
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Company login required.");
        }
        return companyId;
    }

    private Integer requireOwnedRental(Integer rentalId, HttpSession session) {
        Integer companyId = authenticatedCompanyId(session);
        if (rentalId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rental ID is required.");
        }

        var rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found."));

        if (!Objects.equals(rental.getCompanyId(), companyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rental belongs to another company.");
        }
        return companyId;
    }

    @PostMapping
    public ResponseEntity<RentalItemResponse> createRentalItem(
            @RequestBody RentalItemRequest request,
            HttpSession session
    ) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rental item request is required.");
        }

        Integer companyId = requireOwnedRental(request.getRentalId(), session);
        if (request.getEquipmentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Equipment ID is required.");
        }
        if (equipmentRepository.findByIdAndCompanyId(request.getEquipmentId(), companyId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Equipment does not belong to your company.");
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Rental items must be created through rental confirmation."
        );

    }

    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<List<RentalItemResponse>> getRentalItems(
            @PathVariable Integer rentalId,
            HttpSession session
    ) {
        requireOwnedRental(rentalId, session);
        return ResponseEntity.ok(rentalItemService.getRentalItems(rentalId));
    }

    @GetMapping("/{rentalItemId}")
    public ResponseEntity<RentalItemResponse> getRentalItem(
            @PathVariable Integer rentalItemId,
            @RequestParam Integer rentalId,
            HttpSession session
    ) {
        requireOwnedRental(rentalId, session);
        return ResponseEntity.ok(rentalItemService.getRentalItem(rentalItemId, rentalId));
    }

    @PostMapping("/{rentalItemId}/issue")
    public ResponseEntity<RentalItemResponse> issueRentalItem(
            @PathVariable Integer rentalItemId,
            @RequestParam Integer rentalId,
            HttpSession session
    ) {
        Integer companyId = requireOwnedRental(rentalId, session);
        // Reject a mismatched equipment reference before any issue-side mutation.
        RentalItemResponse item = rentalItemService.getRentalItem(rentalItemId, rentalId);
        if (equipmentRepository.findByIdAndCompanyId(item.getEquipmentId(), companyId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rental item equipment belongs to another company.");
        }
        try {
            return ResponseEntity.ok(rentalItemService.issueRentalItem(rentalItemId, rentalId));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }
}
