package com.compulin.rentflow.controller.module2;


import com.compulin.rentflow.dto.module2.CustomerSecondaryContactRequest;
import com.compulin.rentflow.dto.module2.CustomerSecondaryContactResponse;
import com.compulin.rentflow.service.module2.CustomerSecondaryContactService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer-secondary-contacts")
public class CustomerSecondaryContactController {

    private final CustomerSecondaryContactService service;

    public CustomerSecondaryContactController(
            CustomerSecondaryContactService service) {

        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CustomerSecondaryContactResponse> createContact(
            @RequestBody CustomerSecondaryContactRequest request) {

        CustomerSecondaryContactResponse response =
                service.createContact(request);

        if (response == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CustomerSecondaryContactResponse> getContact(
            @PathVariable Integer customerId) {

        CustomerSecondaryContactResponse response =
                service.getContact(customerId);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{secondaryContactId}")
    public ResponseEntity<CustomerSecondaryContactResponse> updateContact(
            @PathVariable Integer secondaryContactId,
            @RequestParam Integer customerId,
            @RequestBody CustomerSecondaryContactRequest request) {

        CustomerSecondaryContactResponse response =
                service.updateContact(
                        secondaryContactId,
                        customerId,
                        request
                );

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
}