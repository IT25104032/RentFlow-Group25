package com.compulin.rentflow.dto.module3;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Invoice breakdown shown to the Rental Officer before the invoice is saved (UC-M3-01, step 9)
public record InvoicePreview(
        Integer rentalId,
        LocalDate startDate,
        LocalDate dueDate,
        long rentalDays,
        List<RentalLine> rentalLines,
        List<AdditionalLine> additionalLines,
        BigDecimal rentalCharges,
        BigDecimal additionalCharges,
        BigDecimal depositHeld,
        BigDecimal depositDeduction,
        BigDecimal totalPayable
) {
    public record RentalLine(Integer rentalItemId, String itemName, Integer quantity,
                             BigDecimal ratePerUnit, String ratePeriod, long units, BigDecimal amount) {}

    public record AdditionalLine(Integer chargeId, String chargeType, String description, BigDecimal amount) {}
}
