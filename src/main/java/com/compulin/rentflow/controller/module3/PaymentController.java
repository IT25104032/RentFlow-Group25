package com.compulin.rentflow.controller.module3;

import com.compulin.rentflow.dto.module3.RecordPaymentRequest;
import com.compulin.rentflow.entity.module3.Payment;
import com.compulin.rentflow.service.module3.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import com.compulin.rentflow.security.Module3Access;
import com.compulin.rentflow.dto.module3.PaymentHistoryItem;
import com.compulin.rentflow.controller.AuthController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final Module3Access access;

    public PaymentController(PaymentService paymentService, Module3Access access) {
        this.paymentService = paymentService;
        this.access = access;
    }

    private void requireRentalOfficer(HttpSession session) {
        Object roleValue = session.getAttribute(AuthController.SESSION_ROLE);

        String role = roleValue == null ? "" : roleValue.toString();

        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        if (!"RENTAL_OFFICER".equals(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only rental officers can record payments."
            );
        }
    }

    // Retrieves all payment records
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments(HttpSession session) {
        access.companyId(session);
        return ResponseEntity.ok(paymentService.getAllPayments().stream()
                .filter(payment -> {
                    try { access.invoice(payment.getInvoice().getInvoiceId(), session); return true; }
                    catch (org.springframework.web.server.ResponseStatusException ex) { return false; }
                }).toList());
    }

    // Processes new payment and automatically updates invoice balance
    @PostMapping
    public ResponseEntity<Payment> recordPayment(@RequestBody RecordPaymentRequest request, HttpSession session) {
        requireRentalOfficer(session);
        access.invoice(request.getInvoiceId(), session);
        Payment processedPayment = paymentService.recordPayment(
                request.getInvoiceId(),
                request.getAmount(),
                request.getPaymentMethod(),
                request.getReferenceNo(),
                access.userId(session)
        );
        return ResponseEntity.ok(processedPayment);
    }

    // Retrieves the payment history for one invoice
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<PaymentHistoryItem>> getPaymentHistory(@PathVariable Integer invoiceId, HttpSession session) {
        access.invoice(invoiceId, session);
        return ResponseEntity.ok(
                paymentService.getPaymentsForInvoice(invoiceId).stream()
                        .map(PaymentHistoryItem::from)
                        .toList());
    }
}