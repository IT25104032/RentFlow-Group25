package com.compulin.rentflow.controller.module2;

import com.compulin.rentflow.dto.module2.RentalConfirmationRequest;
import com.compulin.rentflow.dto.module2.RentalConfirmationResponse;
import com.compulin.rentflow.repository.module2.CustomerRepository;
import com.compulin.rentflow.security.Module1SessionAccess;
import com.compulin.rentflow.service.module2.RentalConfirmationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Objects;

@RestController
@RequestMapping("/api/rentals")
public class RentalConfirmationController {

    private final RentalConfirmationService rentalConfirmationService;
    private final CustomerRepository customerRepository;

    public RentalConfirmationController(
            RentalConfirmationService rentalConfirmationService,
            CustomerRepository customerRepository) {
        this.rentalConfirmationService = rentalConfirmationService;
        this.customerRepository = customerRepository;
    }

    @PostMapping("/confirm")
    public ResponseEntity<RentalConfirmationResponse> confirmRental(
            @RequestBody RentalConfirmationRequest request,
            HttpServletRequest httpRequest) {

        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Please log in first.");
        }

        Integer companyId = Module1SessionAccess.getCompanyId(session);
        Object userIdValue = session.getAttribute("RENTFLOW_USER_ID");
        if (companyId == null || !(userIdValue instanceof Number)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Valid login session required.");
        }

        if (!Module1SessionAccess.hasAnyRole(
                session, "COMPANY_ADMIN", "RENTAL_OFFICER")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Not permitted to create rentals.");
        }

        if (request == null || request.getRental() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Rental details are required.");
        }

        if (!Objects.equals(companyId, request.getRental().getCompanyId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Cannot create a rental for another company.");
        }

        Integer customerId = request.getRental().getCustomerId();
        if (customerId == null || customerRepository
                .findByCustomerIdAndCompanyId(customerId, companyId).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Renter does not belong to this company.");
        }

        // Never trust the creator ID submitted by React/Postman.
        request.getRental().setCreatedBy(((Number) userIdValue).intValue());

        try {
            RentalConfirmationResponse response =
                    rentalConfirmationService.confirmRental(request);

            return ResponseEntity.ok(response);

        } catch (IllegalStateException ex) {

            if (ex.getMessage() != null &&
                    ex.getMessage().startsWith("Equipment issue rejected:")) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        ex.getMessage(),
                        ex
                );
            }

            throw ex;
        }
    }
}
