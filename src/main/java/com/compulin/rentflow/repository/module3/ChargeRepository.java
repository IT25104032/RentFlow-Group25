package com.compulin.rentflow.repository.module3;

import com.compulin.rentflow.entity.module3.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Integer> {

    // Retrieves all charges associated with a specific rental
    List<Charge> findByRentalId(Integer rentalId);

    // Retrieves all charges associated with a specific invoice ID
    List<Charge> findByInvoice_InvoiceId(Integer invoiceId);

    // Finds unbilled charges (where invoice IS NULL) for a rental
    List<Charge> findByRentalIdAndInvoiceIsNull(Integer rentalId);
}
