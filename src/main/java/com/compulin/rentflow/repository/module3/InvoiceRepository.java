package com.compulin.rentflow.repository.module3;

import com.compulin.rentflow.entity.module3.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Spring Boot automatically turns this into: SELECT * FROM invoices WHERE status = ?
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);

    // Spring Boot automatically turns this into: SELECT * FROM invoices WHERE rental_id = ?
    List<Invoice> findByRental_RentalId(Long rentalId);
}
