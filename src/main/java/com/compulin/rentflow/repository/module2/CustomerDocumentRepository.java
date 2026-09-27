package com.compulin.rentflow.repository.module2;

import com.compulin.rentflow.entity.module2.CustomerDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerDocumentRepository
        extends JpaRepository<CustomerDocument, Integer> {

    List<CustomerDocument> findByCustomerId(Integer customerId);

    Optional<CustomerDocument> findByDocumentIdAndCustomerId(
            Integer documentId,
            Integer customerId
    );
}