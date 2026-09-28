package com.compulin.rentflow.repository.module3;

import com.compulin.rentflow.entity.module3.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Integer> {

    // Retrieves all charges associated with a specific invoice
    List<Charge> findByInvoiceId(Integer invoiceId);

    // Finds unbilled charges (where invoice_id IS NULL) for a rental
    List<Charge> findByRentalIdAndInvoiceIdIsNull(Integer rentalId);
}
