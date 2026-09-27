package com.compulin.rentflow.controller.module2;


import com.compulin.rentflow.dto.module2.CustomerDocumentRequest;
import com.compulin.rentflow.dto.module2.CustomerDocumentResponse;
import com.compulin.rentflow.service.module2.CustomerDocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer-documents")
public class CustomerDocumentController {

    private final CustomerDocumentService customerDocumentService;

    public CustomerDocumentController(
            CustomerDocumentService customerDocumentService) {

        this.customerDocumentService = customerDocumentService;
    }

    @PostMapping
    public ResponseEntity<CustomerDocumentResponse> createDocument(
            @RequestBody CustomerDocumentRequest request) {

        CustomerDocumentResponse response =
                customerDocumentService.createDocument(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CustomerDocumentResponse>> getDocumentsByCustomerId(
            @PathVariable Integer customerId) {

        List<CustomerDocumentResponse> documents =
                customerDocumentService.getDocumentsByCustomerId(customerId);

        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<CustomerDocumentResponse> getDocument(
            @PathVariable Integer documentId,
            @RequestParam Integer customerId) {

        CustomerDocumentResponse response =
                customerDocumentService.getDocument(
                        documentId,
                        customerId
                );

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{documentId}")
    public ResponseEntity<CustomerDocumentResponse> updateDocument(
            @PathVariable Integer documentId,
            @RequestParam Integer customerId,
            @RequestBody CustomerDocumentRequest request) {

        CustomerDocumentResponse response =
                customerDocumentService.updateDocument(
                        documentId,
                        customerId,
                        request
                );

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
}
