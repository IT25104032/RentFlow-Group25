package com.compulin.rentflow.controller.module2;

import com.compulin.rentflow.dto.module2.CustomerDocumentResponse;
import com.compulin.rentflow.service.module2.CustomerDocumentService;
import com.compulin.rentflow.repository.module2.CustomerRepository;
import com.compulin.rentflow.security.Module1SessionAccess;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/customer-documents")
public class CustomerDocumentController {

    private final CustomerDocumentService
            customerDocumentService;
    private final CustomerRepository customerRepository;


    public CustomerDocumentController(
            CustomerDocumentService customerDocumentService,
            CustomerRepository customerRepository
    ) {

        this.customerDocumentService =
                customerDocumentService;
        this.customerRepository = customerRepository;
    }

    // Validate ownership before any document lookup, upload, or update.
    private void requireOwnCustomer(Integer customerId, HttpSession session) {
        Integer sessionCompanyId = Module1SessionAccess.getCompanyId(session);
        if (sessionCompanyId == null || session.getAttribute("RENTFLOW_USER_ID") == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required.");
        }
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Renter not found."));
        if (!Objects.equals(customer.getCompanyId(), sessionCompanyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cannot access another company's renter documents.");
        }
    }

    private Integer authenticatedUserId(HttpSession session) {
        Object value = session.getAttribute("RENTFLOW_USER_ID");
        if (!(value instanceof Number)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required.");
        }
        return ((Number) value).intValue();
    }


    /*
     * Create a new identification document.
     */
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CustomerDocumentResponse>
    createDocument(

            @RequestParam Integer customerId,

            @RequestParam String documentType,

            @RequestParam String documentNumber,

            @RequestParam("documentFile")
            MultipartFile documentFile,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate expiryDate,

            @RequestParam Integer checkedBy,

            @RequestParam(required = false)
            String notes,
            HttpSession session
    ) {

        requireOwnCustomer(customerId, session);
        CustomerDocumentResponse response =
                customerDocumentService.createDocument(

                        customerId,

                        documentType,

                        documentNumber,

                        documentFile,

                        expiryDate,

                        authenticatedUserId(session),

                        notes
                );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    /*
     * Get all identification documents
     * belonging to a renter.
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<
            List<CustomerDocumentResponse>
            > getDocumentsByCustomerId(

            @PathVariable Integer customerId,
            HttpSession session
    ) {

        requireOwnCustomer(customerId, session);
        List<CustomerDocumentResponse> documents =
                customerDocumentService
                        .getDocumentsByCustomerId(
                                customerId
                        );

        return ResponseEntity.ok(
                documents
        );
    }


    /*
     * Get one identification document.
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<CustomerDocumentResponse>
    getDocument(

            @PathVariable Integer documentId,

            @RequestParam Integer customerId,
            HttpSession session
    ) {

        requireOwnCustomer(customerId, session);
        CustomerDocumentResponse response =
                customerDocumentService.getDocument(
                        documentId,
                        customerId
                );

        if (response == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                response
        );
    }


    /*
     * Update an existing identification document.
     *
     * Multipart is used because the user may
     * replace the existing physical document.
     */
    @PutMapping(
            value = "/{documentId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CustomerDocumentResponse>
    updateDocument(

            @PathVariable Integer documentId,

            @RequestParam Integer customerId,

            @RequestParam String documentType,

            @RequestParam String documentNumber,

            @RequestParam(
                    value = "documentFile",
                    required = false
            )
            MultipartFile documentFile,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate expiryDate,

            @RequestParam Integer checkedBy,

            @RequestParam(required = false)
            String notes,
            HttpSession session
    ) {

        requireOwnCustomer(customerId, session);
        CustomerDocumentResponse response =
                customerDocumentService.updateDocument(

                        documentId,

                        customerId,

                        documentType,

                        documentNumber,

                        documentFile,

                        expiryDate,

                        authenticatedUserId(session),

                        notes
                );


        if (response == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        return ResponseEntity.ok(
                response
        );
    }
}