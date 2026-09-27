package com.compulin.rentflow.service.module2;


import com.compulin.rentflow.dto.module2.CustomerDocumentRequest;
import com.compulin.rentflow.dto.module2.CustomerDocumentResponse;
import com.compulin.rentflow.entity.module2.CustomerDocument;
import com.compulin.rentflow.repository.module2.CustomerDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerDocumentService {

    private final CustomerDocumentRepository customerDocumentRepository;

    public CustomerDocumentService(
            CustomerDocumentRepository customerDocumentRepository) {

        this.customerDocumentRepository = customerDocumentRepository;
    }

    public CustomerDocumentResponse createDocument(
            CustomerDocumentRequest request) {

        CustomerDocument document = new CustomerDocument();

        document.setCustomerId(request.getCustomerId());
        document.setDocumentType(request.getDocumentType());
        document.setDocumentNumber(request.getDocumentNumber());
        document.setDocumentCopyPath(request.getDocumentCopyPath());
        document.setExpiryDate(request.getExpiryDate());
        document.setCheckedBy(request.getCheckedBy());
        document.setNotes(request.getNotes());

        CustomerDocument savedDocument =
                customerDocumentRepository.save(document);

        return convertToResponse(savedDocument);
    }

    public List<CustomerDocumentResponse> getDocumentsByCustomerId(
            Integer customerId) {

        List<CustomerDocument> documents =
                customerDocumentRepository.findByCustomerId(customerId);

        return documents.stream()
                .map(this::convertToResponse)
                .toList();
    }

    public CustomerDocumentResponse getDocument(
            Integer documentId,
            Integer customerId) {

        Optional<CustomerDocument> optionalDocument =
                customerDocumentRepository
                        .findByDocumentIdAndCustomerId(
                                documentId,
                                customerId
                        );

        if (optionalDocument.isEmpty()) {
            return null;
        }

        return convertToResponse(optionalDocument.get());
    }

    public CustomerDocumentResponse updateDocument(
            Integer documentId,
            Integer customerId,
            CustomerDocumentRequest request) {

        Optional<CustomerDocument> optionalDocument =
                customerDocumentRepository
                        .findByDocumentIdAndCustomerId(
                                documentId,
                                customerId
                        );

        if (optionalDocument.isEmpty()) {
            return null;
        }

        CustomerDocument document = optionalDocument.get();

        document.setDocumentType(request.getDocumentType());
        document.setDocumentNumber(request.getDocumentNumber());
        document.setDocumentCopyPath(request.getDocumentCopyPath());
        document.setExpiryDate(request.getExpiryDate());
        document.setCheckedBy(request.getCheckedBy());
        document.setNotes(request.getNotes());

        CustomerDocument updatedDocument =
                customerDocumentRepository.save(document);

        return convertToResponse(updatedDocument);
    }

    private CustomerDocumentResponse convertToResponse(
            CustomerDocument document) {

        CustomerDocumentResponse response =
                new CustomerDocumentResponse();

        response.setDocumentId(document.getDocumentId());
        response.setCustomerId(document.getCustomerId());
        response.setDocumentType(document.getDocumentType());
        response.setDocumentNumber(document.getDocumentNumber());
        response.setDocumentCopyPath(document.getDocumentCopyPath());
        response.setExpiryDate(document.getExpiryDate());
        response.setCheckedBy(document.getCheckedBy());
        response.setCheckedAt(document.getCheckedAt());
        response.setNotes(document.getNotes());

        return response;
    }
}