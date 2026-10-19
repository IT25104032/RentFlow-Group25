package com.compulin.rentflow.controller.module2;

import com.compulin.rentflow.dto.module2.RentalExtensionRequest;
import com.compulin.rentflow.dto.module2.RentalExtensionResponse;
import com.compulin.rentflow.repository.module2.RentalRepository;
import com.compulin.rentflow.security.Module1SessionAccess;
import com.compulin.rentflow.service.module2.RentalExtensionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/rental-extensions")
public class RentalExtensionController {

    private final RentalExtensionService rentalExtensionService;
    private final RentalRepository rentalRepository;

    public RentalExtensionController(
            RentalExtensionService rentalExtensionService,
            RentalRepository rentalRepository
    ) {
        this.rentalExtensionService = rentalExtensionService;
        this.rentalRepository = rentalRepository;
    }

    private Integer authenticatedCompanyId(HttpSession session) {
        Integer companyId = Module1SessionAccess.getCompanyId(session);
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Company login required.");
        }
        return companyId;
    }

    private Integer authenticatedUserId(HttpSession session) {
        Object userId = session.getAttribute("RENTFLOW_USER_ID");
        if (!(userId instanceof Number)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User login required.");
        }
        return ((Number) userId).intValue();
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
    public ResponseEntity<RentalExtensionResponse> extendRental(
            @RequestBody RentalExtensionRequest request,
            HttpSession session
    ) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Extension request is required.");
        }

        Integer companyId = requireOwnedRental(request.getRentalId(), session);
        Integer userId = authenticatedUserId(session);

        // Never trust company or approver identifiers provided by the client.
        request.setCompanyId(companyId);
        request.setApprovedBy(userId);

        try {
            RentalExtensionResponse response = rentalExtensionService.extendRental(request);
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<List<RentalExtensionResponse>> getExtensionHistory(
            @PathVariable Integer rentalId,
            HttpSession session
    ) {
        requireOwnedRental(rentalId, session);
        return ResponseEntity.ok(rentalExtensionService.getExtensionHistory(rentalId));
    }
}
