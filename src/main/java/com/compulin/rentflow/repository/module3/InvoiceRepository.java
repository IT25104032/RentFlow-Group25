package com.compulin.rentflow.repository.module3;

import com.compulin.rentflow.entity.module3.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    // Finds invoices by status (UNPAID, PARTIALLY_PAID, PAID, CANCELLED)
    List<Invoice> findByInvoiceStatus(Invoice.InvoiceStatus invoiceStatus);

    // Finds all invoices created for a specific rental ID
    List<Invoice> findByRental_RentalId(Integer rentalId);
}