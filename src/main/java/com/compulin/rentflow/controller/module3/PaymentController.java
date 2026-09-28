package com.compulin.rentflow.controller.module3;

import com.compulin.rentflow.dto.module3.RecordPaymentRequest;
import com.compulin.rentflow.entity.module3.Payment;
import com.compulin.rentflow.service.module3.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Retrieves all payment records
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // Processes new payment and automatically updates invoice balance
    @PostMapping
    public ResponseEntity<Payment> recordPayment(@RequestBody RecordPaymentRequest request) {
        Payment processedPayment = paymentService.recordPayment(
                request.getInvoiceId(),
                request.getAmount(),
                request.getPaymentMethod(),
                request.getReferenceNo()
        );
        return ResponseEntity.ok(processedPayment);
    }
}