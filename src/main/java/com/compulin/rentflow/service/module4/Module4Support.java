package com.compulin.rentflow.service.module4;

import com.compulin.rentflow.repository.module4.RentalLookupRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Small helpers shared by the Module 4 services
 * loading a rental for the logged-in company, money rounding and the
 * late-charge rate.
 */
@Component
public class Module4Support {

    public static final Set<String> OPEN_STATUSES =
            Set.of("ACTIVE", "OVERDUE", "PARTIALLY_RETURNED");

    public static final List<String> CONDITIONS =
            List.of("GOOD", "DAMAGED", "MISSING PARTS", "NEEDS MAINTENANCE");

    public static final List<String> DAMAGE_LEVELS =
            List.of("MINOR", "MODERATE", "SEVERE");

    public static final List<String> LOSS_TYPES =
            List.of("LOST", "STOLEN", "NON_RETURNED");

    private final RentalLookupRepository lookup;

    public Module4Support(RentalLookupRepository lookup) {
        this.lookup = lookup;
    }

    /**
     * The rental header, checked against the logged-in user's company
     */
    public Map<String, Object> rental(Integer rentalId, Integer companyId) {
        if (rentalId == null) {
            throw new IllegalArgumentException("Rental ID is required.");
        }
        Map<String, Object> rental = lookup.findRental(rentalId);
        if (rental == null) {
            throw new IllegalArgumentException("Rental #" + rentalId + " was not found.");
        }
        if (companyId != null && !companyId.equals(asInt(rental.get("company_id")))) {
            throw new IllegalArgumentException("Rental #" + rentalId + " belongs to another company.");
        }
        return rental;
    }

    /**
     * Recomputes the Module 2 statuses after a return or a lost note and
     * returns the new rental status.
     */
    public String refreshStatuses(Integer rentalId) {
        List<Map<String, Object>> items = lookup.findIssuedItems(rentalId);
        String current = (String) lookup.findRental(rentalId).get("rental_status");
        if (!OPEN_STATUSES.contains(current) && !"RETURNED".equals(current)) {
            return current;
        }

        boolean anythingBack = false;
        boolean everythingBack = !items.isEmpty();

        for (Map<String, Object> item : items) {
            int issued = asInt(item.get("quantity"));
            int returned = asInt(item.get("returned_qty"));
            int lost = asInt(item.get("lost_qty"));
            int out = issued - returned - lost;

            String status;
            if (out <= 0) {
                status = returned == 0 ? "LOST" : "RETURNED";
            } else if (returned + lost > 0) {
                status = "PARTIALLY_RETURNED";
            } else {
                status = "ISSUED";
            }
            if (!status.equals(item.get("item_status")) && !"CLOSED".equals(item.get("item_status"))) {
                lookup.updateRentalItemStatus(asInt(item.get("rental_item_id")), status);
            }
            anythingBack |= returned + lost > 0;
            everythingBack &= out <= 0;
        }

        String next = current;
        if (everythingBack) {
            next = "RETURNED";
        } else if (anythingBack) {
            next = "PARTIALLY_RETURNED";
        } else if ("RETURNED".equals(current) || "PARTIALLY_RETURNED".equals(current)) {
            next = "ACTIVE";
        }
        if (!next.equals(current)) {
            lookup.updateRentalStatus(rentalId, next);
        }
        return next;
    }

    public static BigDecimal money(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(2)
                : value.setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }


    public static BigDecimal dailyRate(BigDecimal rate, String period) {
        if (rate == null) {
            return BigDecimal.ZERO;
        }
        String p = period == null ? "DAY" : period.trim().toUpperCase();
        return switch (p) {
            case "HOUR", "HOURLY" -> rate.multiply(BigDecimal.valueOf(24));
            case "WEEK", "WEEKLY" -> rate.divide(BigDecimal.valueOf(7), 2, RoundingMode.HALF_UP);
            case "MONTH", "MONTHLY" -> rate.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
            default -> rate;
        };
    }

    public static BigDecimal lateChargePerUnit(BigDecimal rate, String period, long daysLate) {
        if (daysLate <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return money(dailyRate(rate, period).multiply(BigDecimal.valueOf(daysLate)));
    }

    public static int asInt(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    public static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}