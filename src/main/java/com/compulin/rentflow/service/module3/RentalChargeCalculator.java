package com.compulin.rentflow.service.module3;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// Calculates quantity-based rental charges: rate x quantity x rental period
@Component
public class RentalChargeCalculator {

    public enum RatePeriod {
        DAY(1), WEEK(7), MONTH(30);

        private final int daysPerUnit;

        RatePeriod(int daysPerUnit) {
            this.daysPerUnit = daysPerUnit;
        }

        // A part-used week or month is billed as a full one
        public long billableUnits(long days) {
            return (days + daysPerUnit - 1) / daysPerUnit;
        }

        // Accepts DAY/DAILY, WEEK/WEEKLY, MONTH/MONTHLY
        public static RatePeriod from(String value) {
            String v = value == null ? "" : value.trim().toUpperCase();
            if (v.startsWith("WEEK")) return WEEK;
            if (v.startsWith("MONTH")) return MONTH;
            if (v.startsWith("DAY") || v.equals("DAILY")) return DAY;
            throw new IllegalArgumentException("Unknown rate period: " + value);
        }
    }

    // Number of days rented
    public long rentalDays(LocalDate startDate, LocalDate dueDate) {
        long days = ChronoUnit.DAYS.between(startDate, dueDate);
        return Math.max(days, 1);
    }

    public long billableUnits(String ratePeriod, long days) {
        return RatePeriod.from(ratePeriod).billableUnits(days);
    }

    public BigDecimal lineCharge(BigDecimal ratePerUnit, int quantity, long units) {
        return ratePerUnit
                .multiply(BigDecimal.valueOf(quantity))
                .multiply(BigDecimal.valueOf(units));
    }
}

