package com.compulin.rentflow.service.module2;

import com.compulin.rentflow.dto.module2.CustomerDocumentResponse;
import com.compulin.rentflow.entity.module2.CustomerDocument;
import com.compulin.rentflow.repository.module2.CustomerDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerDocumentService {

    private final CustomerDocumentRepository
            customerDocumentRepository;

    private final CustomerDocumentStorageService
            customerDocumentStorageService;


    public CustomerDocumentService(
            CustomerDocumentRepository customerDocumentRepository,
            CustomerDocumentStorageService customerDocumentStorageService
    ) {

        this.customerDocumentRepository =
                customerDocumentRepository;

        this.customerDocumentStorageService =
                customerDocumentStorageService;
    }


    /*
     * Create a new identification document.
     */
    public CustomerDocumentResponse createDocument(
            Integer customerId,
            String documentType,
            String documentNumber,
            MultipartFile documentFile,
            LocalDate expiryDate,
            Integer checkedBy,
            String notes
    ) {

        /*
         * The file is required when creating
         * a new identification document.
         */
        if (documentFile == null ||
                documentFile.isEmpty()) {

            throw new IllegalArgumentException(
                    "Identification document file is required."
            );
        }


        /*
         * Save the actual file and receive
         * the generated server path.
         */
        String documentCopyPath =
                customerDocumentStorageService.saveDocument(
                        documentFile
                );


        CustomerDocument document =
                new CustomerDocument();

        document.setCustomerId(
                customerId
        );

        document.setDocumentType(
                documentType
        );

        document.setDocumentNumber(
                documentNumber
        );

        document.setDocumentCopyPath(
                documentCopyPath
        );

        document.setExpiryDate(
                expiryDate
        );

        document.setCheckedBy(
                checkedBy
        );

        document.setNotes(
                notes
        );


        CustomerDocument savedDocument =
                customerDocumentRepository.save(
                        document
                );


        return convertToResponse(
                savedDocument
        );
    }


    /*
     * Get all identification documents
     * belonging to a renter.
     */
    public List<CustomerDocumentResponse>
    getDocumentsByCustomerId(
            Integer customerId
    ) {

        List<CustomerDocument> documents =
                customerDocumentRepository
                        .findByCustomerId(customerId);

        return documents.stream()
                .map(this::convertToResponse)
                .toList();
    }


    /*
     * Get one identification document.
     */
    public CustomerDocumentResponse getDocument(
            Integer documentId,
            Integer customerId
    ) {

        Optional<CustomerDocument> optionalDocument =
                customerDocumentRepository
                        .findByDocumentIdAndCustomerId(
                                documentId,
                                customerId
                        );

        if (optionalDocument.isEmpty()) {

            return null;
        }

        return convertToResponse(
                optionalDocument.get()
        );
    }


    /*
     * Update an existing identification document.
     *
     * A new file is optional.
     *
     * If a new file is supplied:
     *      save the new file
     *      update document_copy_path
     *
     * If no file is supplied:
     *      keep the existing document_copy_path
     */
    public CustomerDocumentResponse updateDocument(
            Integer documentId,
            Integer customerId,
            String documentType,
            String documentNumber,
            MultipartFile documentFile,
            LocalDate expiryDate,
            Integer checkedBy,
            String notes
    ) {

        Optional<CustomerDocument> optionalDocument =
                customerDocumentRepository
                        .findByDocumentIdAndCustomerId(
                                documentId,
                                customerId
                        );


        if (optionalDocument.isEmpty()) {

            return null;
        }


        CustomerDocument document =
                optionalDocument.get();


        document.setDocumentType(
                documentType
        );

        document.setDocumentNumber(
                documentNumber
        );

        document.setExpiryDate(
                expiryDate
        );

        document.setCheckedBy(
                checkedBy
        );

        document.setNotes(
                notes
        );


        /*
         * Only replace the physical document
         * when the user selected a new file.
         */
        if (documentFile != null &&
                !documentFile.isEmpty()) {

            String newDocumentCopyPath =
                    customerDocumentStorageService
                            .saveDocument(
                                    documentFile
                            );

            document.setDocumentCopyPath(
                    newDocumentCopyPath
            );
        }


        CustomerDocument updatedDocument =
                customerDocumentRepository.save(
                        document
                );


        return convertToResponse(
                updatedDocument
        );
    }


    /*
     * Convert entity into response DTO.
     */
    private CustomerDocumentResponse convertToResponse(
            CustomerDocument document
    ) {

        CustomerDocumentResponse response =
                new CustomerDocumentResponse();

        response.setDocumentId(
                document.getDocumentId()
        );

        response.setCustomerId(
                document.getCustomerId()
        );

        response.setDocumentType(
                document.getDocumentType()
        );

        response.setDocumentNumber(
                document.getDocumentNumber()
        );

        response.setDocumentCopyPath(
                document.getDocumentCopyPath()
        );

        response.setExpiryDate(
                document.getExpiryDate()
        );

        response.setCheckedBy(
                document.getCheckedBy()
        );

        response.setCheckedAt(
                document.getCheckedAt()
        );

        response.setNotes(
                document.getNotes()
        );

        return response;
    }
}