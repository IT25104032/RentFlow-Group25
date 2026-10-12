package com.compulin.rentflow.repository.module3;

import com.compulin.rentflow.entity.module3.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    // Retrieves all payment history records for a specific invoice
    List<Payment> findByInvoice_InvoiceId(Integer invoiceId);

    // Retrieves only active (non-voided) payments towards an invoice balance
    List<Payment> findByInvoice_InvoiceIdAndPaymentStatus(Integer invoiceId, Payment.PaymentStatus paymentStatus);

    // Finds payments recorded by a specific staff member
    List<Payment> findByReceivedBy(Integer receivedBy);
}
