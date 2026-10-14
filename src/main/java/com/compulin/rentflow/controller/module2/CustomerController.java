
package com.compulin.rentflow.controller.module2;

import com.compulin.rentflow.dto.module2.CustomerRequest;
import com.compulin.rentflow.dto.module2.CustomerResponse;
import com.compulin.rentflow.security.Module1SessionAccess;
import com.compulin.rentflow.service.module2.CustomerService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Verify that the requested company belongs
    // to the authenticated Module 1 session.
    private void validateCompany(
            HttpSession session,
            Integer requestedCompanyId) {

        Integer sessionCompanyId =
                Module1SessionAccess.getCompanyId(session);

        if (sessionCompanyId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first."
            );
        }

        if (!Objects.equals(
                sessionCompanyId, requestedCompanyId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access to another company's records is denied."
            );
        }
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request,
            HttpSession session) {

        validateCompany(session, request.getCompanyId());

        Object sessionUserId =
                session.getAttribute("RENTFLOW_USER_ID");

        if (!(sessionUserId instanceof Number)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user was not found."
            );
        }

        // Record the authenticated creator rather
        // than trusting the ID sent by the browser.
        request.setCreatedBy(
                ((Number) sessionUserId).intValue()
        );

        CustomerResponse response =
                customerService.createCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers(
            @RequestParam Integer companyId,
            HttpSession session) {

        validateCompany(session, companyId);

        return ResponseEntity.ok(
                customerService.getAllCustomers(companyId)
        );
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(
            @PathVariable Integer customerId,
            @RequestParam Integer companyId,
            HttpSession session) {

        validateCompany(session, companyId);

        CustomerResponse response =
                customerService.getCustomerById(
                        customerId,
                        companyId
                );

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CustomerResponse>> searchCustomers(
            @RequestParam Integer companyId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            HttpSession session) {

        validateCompany(session, companyId);

        return ResponseEntity.ok(
                customerService.searchCustomers(
                        companyId,
                        name,
                        phone
                )
        );
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Integer customerId,
            @RequestParam Integer companyId,
            @Valid @RequestBody CustomerRequest request,
            HttpSession session) {

        validateCompany(session, companyId);
        validateCompany(session, request.getCompanyId());

        CustomerResponse response =
                customerService.updateCustomer(
                        customerId,
                        companyId,
                        request
                );

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
}
