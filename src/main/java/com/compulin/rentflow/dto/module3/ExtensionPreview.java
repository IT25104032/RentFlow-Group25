package com.compulin.rentflow.dto.module3;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Previous and updated invoice amounts after a rental is extended
public record ExtensionPreview(
        Integer invoiceId,
        Integer rentalId,
        LocalDate startDate,
        LocalDate dueDate,
        long rentalDays,
        List<InvoicePreview.RentalLine> rentalLines,
        BigDecimal previousRentalCharges,
        BigDecimal newRentalCharges,
        BigDecimal additionalCharges,
        BigDecimal depositDeduction,
        BigDecimal previousTotal,
        BigDecimal newTotal,
        BigDecimal amountPaid,
        BigDecimal previousBalance,
        BigDecimal newBalance
) {}
