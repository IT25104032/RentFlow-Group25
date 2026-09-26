package com.compulin.rentflow.controller.module2;

import com.compulin.rentflow.dto.module2.CustomerRequest;
import com.compulin.rentflow.dto.module2.CustomerResponse;
import com.compulin.rentflow.service.module2.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @RequestBody CustomerRequest request) {

        CustomerResponse response = customerService.createCustomer(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(
            @PathVariable Integer customerId,
            @RequestParam Integer companyId) {

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
            @RequestParam(required = false) String phone) {

        List<CustomerResponse> customers =
                customerService.searchCustomers(
                        companyId,
                        name,
                        phone
                );

        return ResponseEntity.ok(customers);
    }
}

