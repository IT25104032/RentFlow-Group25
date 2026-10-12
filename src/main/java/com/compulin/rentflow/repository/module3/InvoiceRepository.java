package com.compulin.rentflow.repository.module3;

import com.compulin.rentflow.entity.module3.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    List<Invoice> findByInvoiceStatus(Invoice.InvoiceStatus invoiceStatus);

    Optional<Invoice> findByRentalId(Integer rentalId);

    // Checks the rental table directly, since the Rental entity belongs to Module 2
    @Query(value = "SELECT COUNT(*) FROM rental WHERE rental_id = :rentalId", nativeQuery = true)
    long countRentalById(@Param("rentalId") Integer rentalId);
}

