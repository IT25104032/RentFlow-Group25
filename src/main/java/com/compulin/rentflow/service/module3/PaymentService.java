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

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public Payment recordPayment(Integer invoiceId, BigDecimal amount, Payment.PaymentMethod paymentMethod, String referenceNo) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero.");
        }

        if (amount.compareTo(invoice.getBalanceDue()) > 0) {
            throw new RuntimeException("Payment exceeds remaining balance. Due: " + invoice.getBalanceDue());
        }

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(paymentMethod);
        payment.setReferenceNo(referenceNo != null ? referenceNo : "TXN-" + System.currentTimeMillis());
        payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);

        Payment savedPayment = paymentRepository.save(payment);

        BigDecimal newAmountPaid = invoice.getAmountPaid().add(amount);
        BigDecimal newBalanceDue = invoice.getTotalAmount().subtract(newAmountPaid);

        invoice.setAmountPaid(newAmountPaid);
        invoice.setBalanceDue(newBalanceDue);

        if (newBalanceDue.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setInvoiceStatus(Invoice.InvoiceStatus.PAID);
        } else {
            invoice.setInvoiceStatus(Invoice.InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);
        return savedPayment;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}
