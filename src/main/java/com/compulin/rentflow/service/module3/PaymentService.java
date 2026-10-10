package com.compulin.rentflow.service.module3;

import com.compulin.rentflow.entity.module3.Invoice;
import com.compulin.rentflow.entity.module3.Payment;
import com.compulin.rentflow.repository.module3.InvoiceRepository;
import com.compulin.rentflow.repository.module3.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          InvoiceRepository invoiceRepository,
                          InvoiceService invoiceService) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceService = invoiceService;
    }

    @Transactional
    public Payment recordPayment(Integer invoiceId, BigDecimal amount, Payment.PaymentMethod paymentMethod, String referenceNo) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero.");
        }

        BigDecimal currentBalance = invoice.getBalanceDue() != null ? invoice.getBalanceDue() : BigDecimal.ZERO;
        if (amount.compareTo(currentBalance) > 0) {
            throw new RuntimeException("Payment exceeds remaining balance. Due: " + currentBalance);
        }

        // 1. Record the new payment transaction
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(paymentMethod);
        payment.setReferenceNo(referenceNo != null && !referenceNo.trim().isEmpty()
                ? referenceNo
                : "TXN-" + System.currentTimeMillis());
        payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);

        Payment savedPayment = paymentRepository.save(payment);

        // 2. Update total amount paid on the invoice (with null check)
        BigDecimal existingPaid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
        invoice.setAmountPaid(existingPaid.add(amount));
        invoiceRepository.save(invoice);

        // 3. Delegate balance and status recalculation to InvoiceService
        invoiceService.recalculateInvoiceTotals(invoiceId);

        return savedPayment;
    }


     //Get all payment records in the system

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }


     //Get payment history for a specific invoice

    public List<Payment> getPaymentsByInvoiceId(Integer invoiceId) {
        return paymentRepository.findByInvoice_InvoiceId(invoiceId);
    }

    // Retrieves all payments recorded against an invoice, oldest first
    public List<Payment> getPaymentsForInvoice(Integer invoiceId) {
        List<Payment> payments = paymentRepository.findByInvoice_InvoiceId(invoiceId);
        payments.sort(java.util.Comparator.comparing(Payment::getPaymentDate));
        return payments;
    }
}
