package com.compulin.rentflow.repository.module3;

import com.compulin.rentflow.entity.module3.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Long> {

    // Retrieves all itemized charges that belong to a specific invoice
    List<Charge> findByInvoice_InvoiceId(Long invoiceId);

    // CRITICAL FOR MODULE 3: Finds all unbilled penalties from Module 4
    // (like damage or late fees) so you can attach them to a new invoice.
    List<Charge> findByRental_RentalIdAndInvoiceIsNull(Long rentalId);
}
