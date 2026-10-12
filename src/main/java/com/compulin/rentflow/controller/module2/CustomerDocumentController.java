package com.compulin.rentflow.controller.module2;

import com.compulin.rentflow.dto.module2.CustomerDocumentResponse;
import com.compulin.rentflow.service.module2.CustomerDocumentService;
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


    public CustomerDocumentController(
            CustomerDocumentService customerDocumentService
    ) {

        this.customerDocumentService =
                customerDocumentService;
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
            String notes
    ) {

        CustomerDocumentResponse response =
                customerDocumentService.createDocument(

                        customerId,

                        documentType,

                        documentNumber,

                        documentFile,

                        expiryDate,

                        checkedBy,

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

            @PathVariable Integer customerId
    ) {

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

            @RequestParam Integer customerId
    ) {

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
            String notes
    ) {

        CustomerDocumentResponse response =
                customerDocumentService.updateDocument(

                        documentId,

                        customerId,

                        documentType,

                        documentNumber,

                        documentFile,

                        expiryDate,

                        checkedBy,

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