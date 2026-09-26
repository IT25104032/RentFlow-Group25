package com.compulin.rentflow.entity.module3;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long invoiceId;

    // Assumes Member 2 has created the Rental entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "additional_charges", precision = 12, scale = 2)
    private BigDecimal additionalCharges = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "amount_paid", precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "balance_due", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceDue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 25)
    private InvoiceStatus status;

    // Constructors, Getters, and Setters omitted for brevity.
    // If you use Lombok, just add @Data, @NoArgsConstructor, and @AllArgsConstructor to the top of the class.

    public enum InvoiceStatus {
        UNPAID, PARTIALLY_PAID, PAID, CANCELLED
    }

}
