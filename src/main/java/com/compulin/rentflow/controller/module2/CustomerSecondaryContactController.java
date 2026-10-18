package com.compulin.rentflow.controller.module2;

import com.compulin.rentflow.dto.module2.CustomerSecondaryContactRequest;
import com.compulin.rentflow.dto.module2.CustomerSecondaryContactResponse;
import com.compulin.rentflow.repository.module2.CustomerRepository;
import com.compulin.rentflow.security.Module1SessionAccess;
import com.compulin.rentflow.service.module2.CustomerSecondaryContactService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@RestController
@RequestMapping("/api/customer-secondary-contacts")
public class CustomerSecondaryContactController {

    private final CustomerSecondaryContactService service;
    private final CustomerRepository customerRepository;

    public CustomerSecondaryContactController(
            CustomerSecondaryContactService service,
            CustomerRepository customerRepository) {
        this.service = service;
        this.customerRepository = customerRepository;
    }

    private void requireCustomerInSessionCompany(Integer customerId, HttpSession session) {
        Integer companyId = Module1SessionAccess.getCompanyId(session);
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Company login required.");
        }
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer ID is required.");
        }
        if (customerRepository.findByCustomerIdAndCompanyId(customerId, companyId).isEmpty()) {
            // Do not reveal whether the customer exists under a different company.
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer access denied.");
        }
    }

    @PostMapping
    public ResponseEntity<CustomerSecondaryContactResponse> createContact(
            @Valid @RequestBody CustomerSecondaryContactRequest request,
            HttpSession session) {
        requireCustomerInSessionCompany(request.getCustomerId(), session);

        CustomerSecondaryContactResponse response = service.createContact(request);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CustomerSecondaryContactResponse> getContact(
            @PathVariable Integer customerId,
            HttpSession session) {
        requireCustomerInSessionCompany(customerId, session);

        CustomerSecondaryContactResponse response = service.getContact(customerId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{secondaryContactId}")
    public ResponseEntity<CustomerSecondaryContactResponse> updateContact(
            @PathVariable Integer secondaryContactId,
            @RequestParam Integer customerId,
            @Valid @RequestBody CustomerSecondaryContactRequest request,
            HttpSession session) {
        requireCustomerInSessionCompany(customerId, session);
        if (request.getCustomerId() != null && !Objects.equals(request.getCustomerId(), customerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer ID mismatch.");
        }

        CustomerSecondaryContactResponse response =
                service.updateContact(secondaryContactId, customerId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}
