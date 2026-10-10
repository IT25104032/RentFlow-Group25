package com.compulin.rentflow.service.module3;

import com.compulin.rentflow.dto.module3.ExtensionPreview;
import com.compulin.rentflow.dto.module3.InvoicePreview;
import com.compulin.rentflow.entity.module3.Charge;
import com.compulin.rentflow.entity.module3.Invoice;
import com.compulin.rentflow.entity.module3.SecurityDeposit;
import com.compulin.rentflow.exception.module3.DuplicateInvoiceException;
import com.compulin.rentflow.exception.module3.InvoiceGenerationException;
import com.compulin.rentflow.exception.module3.ResourceNotFoundException;
import com.compulin.rentflow.repository.module3.ChargeRepository;
import com.compulin.rentflow.repository.module3.InvoiceRepository;
import com.compulin.rentflow.repository.module3.RentalBillingRepository;
import com.compulin.rentflow.repository.module3.RentalBillingRepository.RentalItemLine;
import com.compulin.rentflow.repository.module3.RentalBillingRepository.RentalPeriod;
import com.compulin.rentflow.repository.module3.SecurityDepositRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ChargeRepository chargeRepository;
    private final SecurityDepositRepository securityDepositRepository;
    private final RentalBillingRepository rentalBillingRepository;
    private final RentalChargeCalculator chargeCalculator;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          ChargeRepository chargeRepository,
                          SecurityDepositRepository securityDepositRepository,
                          RentalBillingRepository rentalBillingRepository,
                          RentalChargeCalculator chargeCalculator) {
        this.invoiceRepository = invoiceRepository;
        this.chargeRepository = chargeRepository;
        this.securityDepositRepository = securityDepositRepository;
        this.rentalBillingRepository = rentalBillingRepository;
        this.chargeCalculator = chargeCalculator;
    }

    // Builds the invoice breakdown without saving anything
    public InvoicePreview previewInvoice(Integer rentalId) {
        // Make sure the rental exists before creating an invoice for it
        RentalPeriod rental = rentalBillingRepository.findRentalPeriod(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found with ID: " + rentalId));

        // Prevent duplicate invoices for the same rental
        invoiceRepository.findByRentalId(rentalId).ifPresent(existing -> {
            throw new DuplicateInvoiceException("An invoice already exists for Rental ID: " + rentalId
                    + " (Invoice #" + existing.getInvoiceId() + ")");
        });

        // Rental information must be complete
        if (rental.startDate() == null || rental.dueDate() == null) {
            throw new InvoiceGenerationException("Rental #" + rentalId + " has no rental period recorded.");
        }
        List<RentalItemLine> items = rentalBillingRepository.findRentalItems(rentalId);
        if (items.isEmpty()) {
            throw new InvoiceGenerationException("Rental #" + rentalId + " has no rental items recorded.");
        }

        // Step 5 rate x quantity x rental period for each item
        long days = chargeCalculator.rentalDays(rental.startDate(), rental.dueDate());
        List<InvoicePreview.RentalLine> rentalLines = calculateRentalLines(items, days);
        BigDecimal rentalCharges = sumLines(rentalLines);

        // Step 6 late, damage, lost-item and other charges recorded for this rental but not billed yet
        List<InvoicePreview.AdditionalLine> additionalLines = new ArrayList<>();
        BigDecimal additionalCharges = BigDecimal.ZERO;
        for (Charge charge : chargeRepository.findByRentalIdAndInvoiceIsNull(rentalId)) {
            if (charge.getChargeType() == Charge.ChargeType.RENTAL) {
                continue; // rental charges are calculated above, not taken from old charge rows
            }
            additionalLines.add(new InvoicePreview.AdditionalLine(charge.getChargeId(),
                    charge.getChargeType().name(), charge.getChargeDescription(), charge.getAmount()));
            additionalCharges = additionalCharges.add(charge.getAmount());
        }

        // Step 7 deduct the security deposit still held, without the total going below zero
        BigDecimal depositHeld = heldDepositFor(rentalId);
        BigDecimal grossTotal = rentalCharges.add(additionalCharges);
        BigDecimal depositDeduction = depositHeld.min(grossTotal);

        // Step 8 Total = Rental Charges + Additional Charges - Security Deposit Deduction
        BigDecimal totalPayable = grossTotal.subtract(depositDeduction);

        return new InvoicePreview(rentalId, rental.startDate(), rental.dueDate(), days,
                rentalLines, additionalLines, rentalCharges, additionalCharges,
                depositHeld, depositDeduction, totalPayable);
    }

    // Saves the invoice after the Rental Officer confirms it
    @Transactional
    public Invoice generateInvoiceForRental(Integer rentalId) {
        InvoicePreview preview = previewInvoice(rentalId);
        RentalPeriod rental = rentalBillingRepository.findRentalPeriod(rentalId).orElseThrow();

        Invoice invoice = new Invoice();
        invoice.setRentalId(rentalId);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(14)); // Default 14-day payment terms
        invoice.setSubtotal(preview.rentalCharges());
        invoice.setAdditionalCharges(preview.additionalCharges());
        invoice.setTotalAmount(preview.totalPayable());
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setBalanceDue(preview.totalPayable());
        invoice.setInvoiceStatus(preview.totalPayable().compareTo(BigDecimal.ZERO) == 0
                ? Invoice.InvoiceStatus.PAID
                : Invoice.InvoiceStatus.UNPAID);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Record one RENTAL charge per item so the receipt is itemized
        for (InvoicePreview.RentalLine line : preview.rentalLines()) {
            Charge charge = new Charge();
            charge.setRentalId(rentalId);
            charge.setRentalItemId(line.rentalItemId());
            charge.setInvoice(savedInvoice);
            charge.setChargeType(Charge.ChargeType.RENTAL);
            charge.setChargeDescription(describe(line));
            charge.setAmount(line.amount());
            charge.setChargeDate(LocalDateTime.now());
            charge.setCreatedBy(rental.createdBy());
            chargeRepository.save(charge);
        }

        // Attach the additional charges to this invoice
        for (InvoicePreview.AdditionalLine line : preview.additionalLines()) {
            chargeRepository.findById(line.chargeId()).ifPresent(charge -> {
                charge.setInvoice(savedInvoice);
                chargeRepository.save(charge);
            });
        }

        // Mark the deposit amount as used by this invoice, so it can't also be refunded
        applyDepositToInvoice(rentalId, preview.depositDeduction());

        return savedInvoice;
    }

    // Records the deduction on the deposit: only the remainder can still be refunded
    private void applyDepositToInvoice(Integer rentalId, BigDecimal deduction) {
        if (deduction.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        securityDepositRepository.findByRentalId(rentalId)
                .filter(d -> d.getDepositStatus() == SecurityDeposit.DepositStatus.HELD)
                .ifPresent(deposit -> {
                    BigDecimal newDeducted = orZero(deposit.getAmountDeducted()).add(deduction);
                    deposit.setAmountDeducted(newDeducted);

                    BigDecimal remaining = orZero(deposit.getDepositAmountReceived())
                            .subtract(newDeducted)
                            .subtract(orZero(deposit.getAmountRefunded()));
                    if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                        deposit.setDepositStatus(SecurityDeposit.DepositStatus.FORFEITED);
                    }
                    securityDepositRepository.save(deposit);
                });
    }

    // rate x quantity x rental period for each item
    private List<InvoicePreview.RentalLine> calculateRentalLines(List<RentalItemLine> items, long days) {
        List<InvoicePreview.RentalLine> lines = new ArrayList<>();
        for (RentalItemLine item : items) {
            long units;
            try {
                units = chargeCalculator.billableUnits(item.ratePeriod(), days);
            } catch (IllegalArgumentException ex) {
                throw new InvoiceGenerationException("Rental item #" + item.rentalItemId() + ": " + ex.getMessage());
            }
            BigDecimal amount = chargeCalculator.lineCharge(item.ratePerUnit(), item.quantity(), units);
            lines.add(new InvoicePreview.RentalLine(item.rentalItemId(), item.itemName(), item.quantity(),
                    item.ratePerUnit(), item.ratePeriod(), units, amount));
        }
        return lines;
    }

    private static BigDecimal sumLines(List<InvoicePreview.RentalLine> lines) {
        return lines.stream().map(InvoicePreview.RentalLine::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String describe(InvoicePreview.RentalLine line) {
        return line.itemName() + " (" + line.quantity() + " x Rs. " + line.ratePerUnit()
                + " x " + line.units() + " " + line.ratePeriod().toLowerCase() + ")";
    }

    // Shows the previous and updated amounts after a rental is extended, without saving
    public ExtensionPreview previewExtensionUpdate(Integer invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);
        if (invoice.getInvoiceStatus() == Invoice.InvoiceStatus.CANCELLED) {
            throw new InvoiceGenerationException("Invoice #" + invoiceId + " is cancelled.");
        }

        RentalPeriod rental = rentalBillingRepository.findRentalPeriod(invoice.getRentalId())
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found with ID: " + invoice.getRentalId()));
        List<RentalItemLine> items = rentalBillingRepository.findRentalItems(invoice.getRentalId());
        if (items.isEmpty()) {
            throw new InvoiceGenerationException("Rental #" + invoice.getRentalId() + " has no rental items recorded.");
        }

        // Step 4 recalculate each item for the revised rental period
        long days = chargeCalculator.rentalDays(rental.startDate(), rental.dueDate());
        List<InvoicePreview.RentalLine> newLines = calculateRentalLines(items, days);
        BigDecimal newRentalCharges = sumLines(newLines);
        BigDecimal previousRentalCharges = orZero(invoice.getSubtotal());

        // The rental period hasn't changed, so there is nothing to update
        if (newRentalCharges.compareTo(previousRentalCharges) == 0) {
            throw new InvoiceGenerationException("No change in the rental period for Rental #" + invoice.getRentalId()
                    + ". Record the extension in the rental first (new due date).");
        }

        // Steps 5 and 6 keep additional charges and the deposit deduction, subtract payments already made
        BigDecimal additional = orZero(invoice.getAdditionalCharges());
        BigDecimal deduction = invoice.getDepositDeduction().min(newRentalCharges.add(additional));
        BigDecimal newTotal = newRentalCharges.add(additional).subtract(deduction);
        BigDecimal amountPaid = orZero(invoice.getAmountPaid());

        return new ExtensionPreview(invoiceId, invoice.getRentalId(), rental.startDate(), rental.dueDate(), days,
                newLines, previousRentalCharges, newRentalCharges, additional, deduction,
                orZero(invoice.getTotalAmount()), newTotal, amountPaid,
                orZero(invoice.getBalanceDue()), newTotal.subtract(amountPaid));
    }

    // Saves the recalculated rental charges after the Rental Officer confirms
    @Transactional
    public Invoice applyExtensionUpdate(Integer invoiceId) {
        ExtensionPreview preview = previewExtensionUpdate(invoiceId);
        Invoice invoice = getInvoiceById(invoiceId);
        RentalPeriod rental = rentalBillingRepository.findRentalPeriod(invoice.getRentalId()).orElseThrow();

        List<Charge> existing = chargeRepository.findByInvoice_InvoiceId(invoiceId);
        for (InvoicePreview.RentalLine line : preview.rentalLines()) {
            // Update the item's RENTAL charge, or add one if it doesn't have one yet
            Charge charge = existing.stream()
                    .filter(c -> c.getChargeType() == Charge.ChargeType.RENTAL
                            && line.rentalItemId().equals(c.getRentalItemId()))
                    .findFirst()
                    .orElseGet(() -> {
                        Charge c = new Charge();
                        c.setRentalId(invoice.getRentalId());
                        c.setRentalItemId(line.rentalItemId());
                        c.setInvoice(invoice);
                        c.setChargeType(Charge.ChargeType.RENTAL);
                        c.setCreatedBy(rental.createdBy());
                        return c;
                    });
            charge.setChargeDescription(describe(line));
            charge.setAmount(line.amount());
            charge.setChargeDate(LocalDateTime.now());
            chargeRepository.save(charge);
        }

        // Step 9 totals, balance and status follow from the updated charges
        return recalculateInvoiceTotals(invoiceId);
    }

    // Deposit still held for the rental = received - already deducted - already refunded
    private BigDecimal heldDepositFor(Integer rentalId) {
        return securityDepositRepository.findByRentalId(rentalId)
                .filter(d -> d.getDepositStatus() == SecurityDeposit.DepositStatus.HELD)
                .map(d -> orZero(d.getDepositAmountReceived())
                        .subtract(orZero(d.getAmountDeducted()))
                        .subtract(orZero(d.getAmountRefunded()))
                        .max(BigDecimal.ZERO))
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    // Retrieves an invoice by primary key
    public Invoice getInvoiceById(Integer id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));
    }

    // Retrieves an invoice by linked rental ID
    public Invoice getInvoiceByRentalId(Integer rentalId) {
        return invoiceRepository.findByRentalId(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("No invoice found for Rental ID: " + rentalId));
    }

    // Retrieves all invoices
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    // Recalculates subtotal, additional charges, total amount, and balance due when fees are updated
    @Transactional
    public Invoice recalculateInvoiceTotals(Integer invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);
        List<Charge> charges = chargeRepository.findByInvoice_InvoiceId(invoiceId);

        BigDecimal subtotal = charges.stream()
                .filter(c -> c.getChargeType() == Charge.ChargeType.RENTAL)
                .map(Charge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal additionalCharges = charges.stream()
                .filter(c -> c.getChargeType() != Charge.ChargeType.RENTAL)
                .map(Charge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Keep the deposit deduction
        // but never let it push the total below zero
        BigDecimal grossTotal = subtotal.add(additionalCharges);
        BigDecimal depositDeduction = invoice.getDepositDeduction().min(grossTotal);
        BigDecimal totalAmount = grossTotal.subtract(depositDeduction);
        BigDecimal amountPaid = orZero(invoice.getAmountPaid());
        BigDecimal balanceDue = totalAmount.subtract(amountPaid);

        invoice.setSubtotal(subtotal);
        invoice.setAdditionalCharges(additionalCharges);
        invoice.setTotalAmount(totalAmount);
        invoice.setBalanceDue(balanceDue);

        // Auto-update status based on balance
        if (invoice.getInvoiceStatus() != Invoice.InvoiceStatus.CANCELLED) {
            if (balanceDue.compareTo(BigDecimal.ZERO) <= 0) {
                invoice.setInvoiceStatus(Invoice.InvoiceStatus.PAID);
            } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
                invoice.setInvoiceStatus(Invoice.InvoiceStatus.PARTIALLY_PAID);
            } else {
                invoice.setInvoiceStatus(Invoice.InvoiceStatus.UNPAID);
            }
        }

        return invoiceRepository.save(invoice);
    }

    // Cancels an existing invoice
    @Transactional
    public Invoice cancelInvoice(Integer invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);
        invoice.setInvoiceStatus(Invoice.InvoiceStatus.CANCELLED);
        return invoiceRepository.save(invoice);
    }
}
