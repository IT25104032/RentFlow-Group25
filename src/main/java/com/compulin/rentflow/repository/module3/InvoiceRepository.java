package com.compulin.rentflow.repository.module3;

import com.compulin.rentflow.entity.module3.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    List<Invoice> findByInvoiceStatus(Invoice.InvoiceStatus invoiceStatus);

    // Change List<Invoice> to Optional<Invoice>
    Optional<Invoice> findByRentalId(Integer rentalId);
}