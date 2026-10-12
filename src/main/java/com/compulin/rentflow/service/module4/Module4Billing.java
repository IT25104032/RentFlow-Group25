package com.compulin.rentflow.service.module4;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/*
 * Billing (Module 3 tables)
 * Every amount Module 4 bills (late return, damage, lost item) is saved as a
 * charge on the rental's invoice through Module4Links.
 */
@Component
public class Module4Billing {

    public static final String LATE = "LATE";
    public static final String DAMAGE = "DAMAGE";
    public static final String LOST_ITEM = "LOST_ITEM";

    private final Module4Links links;

    public Module4Billing(Module4Links links) {
        this.links = links;
    }

    public void addCharge(Integer rentalId, Integer rentalItemId, String type,
                          String description, BigDecimal amount, Integer userId) {
        if (!Module4Support.isPositive(amount)) {
            return;
        }
        String text = description.length() > 255 ? description.substring(0, 255) : description;
        links.addCharge(rentalId, rentalItemId, type, text, Module4Support.money(amount), userId);
    }

    /** Removes the charges created for one damage record / lost note and re-totals the invoice. */
    public BigDecimal removeCharges(Integer rentalId, String type, String label) {
        return links.removeCharges(rentalId, type, label);
    }

    public static String damageLabel(Integer damageId) {
        return "Damage #" + damageId;
    }

    public static String lostLabel(Integer lostItemId) {
        return "Lost #" + lostItemId;
    }
}