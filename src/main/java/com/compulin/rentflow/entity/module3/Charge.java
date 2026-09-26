package com.compulin.rentflow.entity.module3;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "charges")
public class Charge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "charge_id")
    private Long chargeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    // Allowed to be null because a general charge might not belong to a specific item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_item_id")
    private RentalItem rentalItem;

    // Allowed to be null because charges can be recorded before an invoice is generated
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_type", nullable = false, length = 30)
    private ChargeType chargeType;

    @Column(name = "description")
    private String description;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "charge_date", nullable = false)
    private LocalDateTime chargeDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // If using Lombok, use @Data, @NoArgsConstructor, @AllArgsConstructor.
    // Otherwise, generate Getters and Setters here.

    public enum ChargeType {
        RENTAL, EXTENSION, LATE, DAMAGE, LOST_ITEM, OTHER
    }
}
