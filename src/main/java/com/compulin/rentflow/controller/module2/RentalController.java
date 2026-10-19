package com.compulin.rentflow.controller.module2;

import com.compulin.rentflow.dto.module2.RentalRequest;
import com.compulin.rentflow.dto.module2.RentalResponse;
import com.compulin.rentflow.dto.module2.RentalHistoryResponse;
import com.compulin.rentflow.service.module2.RentalService;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.server.ResponseStatusException;
import com.compulin.rentflow.security.Module1SessionAccess;
import com.compulin.rentflow.repository.module2.CustomerRepository;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService rentalService;
    private final CustomerRepository customerRepository;

    public RentalController(RentalService rentalService, CustomerRepository customerRepository) {
        this.rentalService = rentalService;
        this.customerRepository = customerRepository;
    }

    private void validateCompany(HttpSession session, Integer companyId) {
        Integer authenticatedCompany = session == null ? null : Module1SessionAccess.getCompanyId(session);
        if (authenticatedCompany == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        if (!Objects.equals(authenticatedCompany, companyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Another company's data is not accessible");
        }
    }

    @PostMapping
    public ResponseEntity<RentalResponse> createRental(
            @RequestBody RentalRequest request,
            HttpSession session) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rental details required");
        }
        validateCompany(session, request.getCompanyId());
        if (request.getCustomerId() == null || customerRepository
                .findByCustomerIdAndCompanyId(request.getCustomerId(), request.getCompanyId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Renter does not belong to this company");
        }
        Object userId = session.getAttribute("RENTFLOW_USER_ID");
        if (!(userId instanceof Number number)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Valid user required");
        }
        request.setCreatedBy(number.intValue());

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
            @RequestParam Integer companyId, HttpSession session) {
        validateCompany(session, companyId);

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
            @RequestParam Integer companyId, HttpSession session) {
        validateCompany(session, companyId);

        List<RentalHistoryResponse> history =
                rentalService.getCustomerRentalHistory(
                        customerId,
                        companyId
                );

        return ResponseEntity.ok(history);
    }

    @GetMapping("/active")
    public ResponseEntity<List<RentalResponse>> getActiveRentals(
            @RequestParam Integer companyId, HttpSession session) {
        validateCompany(session, companyId);

        return ResponseEntity.ok(
                rentalService.getActiveRentals(companyId)
        );
    }

    @GetMapping("/for-issue")
    public ResponseEntity<List<RentalResponse>> getRentalsForIssue(
            @RequestParam Integer companyId, HttpSession session) {
        validateCompany(session, companyId);

        return ResponseEntity.ok(
                rentalService.getRentalsForIssue(companyId)
        );
    }

    @GetMapping("/for-extension")
    public ResponseEntity<List<RentalResponse>> getRentalsForExtension(
            @RequestParam Integer companyId, HttpSession session) {
        validateCompany(session, companyId);

        return ResponseEntity.ok(
                rentalService.getRentalsForExtension(companyId)
        );
    }

    @PostMapping("/{rentalId}/issue")
    public ResponseEntity<?> issueRental(
            @PathVariable Integer rentalId,
            @RequestParam Integer companyId, HttpSession session) {
        validateCompany(session, companyId);

        try {
            return ResponseEntity.ok(
                    rentalService.issueRental(rentalId, companyId)
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    @PostMapping("/{rentalId}/cancel")
    public ResponseEntity<?> cancelRental(
            @PathVariable Integer rentalId,
            @RequestParam Integer companyId, HttpSession session) {
        validateCompany(session, companyId);

        try {
            return ResponseEntity.ok(
                    rentalService.cancelRental(rentalId, companyId)
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<RentalResponse>> getOverdueRentals(
            @RequestParam Integer companyId, HttpSession session) {
        validateCompany(session, companyId);

        return ResponseEntity.ok(
                rentalService.getOverdueRentals(companyId)
        );
    }

    @GetMapping("/due-soon")
    public ResponseEntity<List<RentalResponse>> getRentalsDueSoon(
            @RequestParam Integer companyId, HttpSession session) {
        validateCompany(session, companyId);

        return ResponseEntity.ok(
                rentalService.getRentalsDueSoon(companyId)
        );
    }
}
