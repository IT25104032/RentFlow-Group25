package com.compulin.rentflow.entity.module3;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "charge")
public class Charge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "charge_id")
    private Integer chargeId;

    @Column(name = "rental_id", nullable = false)
    private Integer rentalId;

    @Column(name = "rental_item_id")
    private Integer rentalItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_type", nullable = false, length = 30)
    private ChargeType chargeType;

    @Column(name = "charge_description", length = 255)
    private String chargeDescription;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "charge_date", nullable = false)
    private LocalDateTime chargeDate;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    public enum ChargeType {
        RENTAL, EXTENSION, LATE, DAMAGE, LOST_ITEM, OTHER
    }

    public Charge() {
    }

    public Charge(Integer rentalId, Integer rentalItemId, Invoice invoice, ChargeType chargeType,
                  String chargeDescription, BigDecimal amount, LocalDateTime chargeDate, Integer createdBy) {
        this.rentalId = rentalId;
        this.rentalItemId = rentalItemId;
        this.invoice = invoice;
        this.chargeType = chargeType;
        this.chargeDescription = chargeDescription;
        this.amount = amount;
        this.chargeDate = chargeDate;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Integer getChargeId() {

        return chargeId;
    }

    public void setChargeId(Integer chargeId) {

        this.chargeId = chargeId;
    }

    public Integer getRentalId() {

        return rentalId;
    }

    public void setRentalId(Integer rentalId) {

        this.rentalId = rentalId;
    }

    public Integer getRentalItemId() {

        return rentalItemId;
    }

    public void setRentalItemId(Integer rentalItemId) {

        this.rentalItemId = rentalItemId;
    }

    public Invoice getInvoice() {

        return invoice;
    }

    public void setInvoice(Invoice invoice) {

        this.invoice = invoice;
    }

    public ChargeType getChargeType() {

        return chargeType;
    }

    public void setChargeType(ChargeType chargeType) {

        this.chargeType = chargeType;
    }

    public String getChargeDescription() {

        return chargeDescription;
    }

    public void setChargeDescription(String chargeDescription) {

        this.chargeDescription = chargeDescription;
    }

    public BigDecimal getAmount() {

        return amount;
    }

    public void setAmount(BigDecimal amount) {

        this.amount = amount;
    }

    public LocalDateTime getChargeDate() {

        return chargeDate;
    }

    public void setChargeDate(LocalDateTime chargeDate) {

        this.chargeDate = chargeDate;
    }

    public Integer getCreatedBy() {

        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {

        this.createdBy = createdBy;
    }
}