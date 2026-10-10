package com.compulin.rentflow.service.module3;

import com.compulin.rentflow.entity.module3.Charge;
import com.compulin.rentflow.entity.module3.Invoice;
import com.compulin.rentflow.repository.module3.ChargeRepository;
import com.compulin.rentflow.repository.module3.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.compulin.rentflow.exception.module3.DuplicateInvoiceException;
import com.compulin.rentflow.exception.module3.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ChargeRepository chargeRepository;

    @Autowired
    public InvoiceService(InvoiceRepository invoiceRepository, ChargeRepository chargeRepository) {
        this.invoiceRepository = invoiceRepository;
        this.chargeRepository = chargeRepository;
    }

    // Generates a new invoice for a given rental ID with base initial values
    @Transactional
    public Invoice generateInvoiceForRental(Integer rentalId) {
        // Make sure the rental exists before creating an invoice for it
        if (invoiceRepository.countRentalById(rentalId) == 0) {
            throw new ResourceNotFoundException("Rental not found with ID: " + rentalId);
        }

        // Prevent duplicate invoices for the same rental
        invoiceRepository.findByRentalId(rentalId).ifPresent(existing -> {
            throw new DuplicateInvoiceException("An invoice already exists for Rental ID: " + rentalId);
        });

        // Sum initial base charges (RENTAL type charges) attached to this rental
        List<Charge> charges = chargeRepository.findByRentalId(rentalId);
        BigDecimal subtotal = charges.stream()
                .filter(c -> c.getChargeType() == Charge.ChargeType.RENTAL)
                .map(Charge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal additionalCharges = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(additionalCharges);
        BigDecimal amountPaid = BigDecimal.ZERO;
        BigDecimal balanceDue = totalAmount.subtract(amountPaid);

        Invoice invoice = new Invoice();
        invoice.setRentalId(rentalId);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(14)); // Default 14-day payment terms
        invoice.setSubtotal(subtotal);
        invoice.setAdditionalCharges(additionalCharges);
        invoice.setTotalAmount(totalAmount);
        invoice.setAmountPaid(amountPaid);
        invoice.setBalanceDue(balanceDue);
        invoice.setInvoiceStatus(Invoice.InvoiceStatus.UNPAID);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Associate existing charges with this newly created invoice ID
        for (Charge charge : charges) {
            charge.setInvoice(savedInvoice);
            chargeRepository.save(charge);
        }

        return savedInvoice;
    }

    // Retrieves an invoice by primary key
    public Invoice getInvoiceById(Integer id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + id));
    }

    // Retrieves an invoice by linked rental ID
    public Invoice getInvoiceByRentalId(Integer rentalId) {
        return invoiceRepository.findByRentalId(rentalId)
                .orElseThrow(() -> new RuntimeException("No invoice found for Rental ID: " + rentalId));
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

        BigDecimal totalAmount = subtotal.add(additionalCharges);
        BigDecimal balanceDue = totalAmount.subtract(invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO);

        invoice.setSubtotal(subtotal);
        invoice.setAdditionalCharges(additionalCharges);
        invoice.setTotalAmount(totalAmount);
        invoice.setBalanceDue(balanceDue);

        // Auto-update status based on balance
        if (balanceDue.compareTo(BigDecimal.ZERO) <= 0 && invoice.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            invoice.setInvoiceStatus(Invoice.InvoiceStatus.PAID);
        } else if (invoice.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            invoice.setInvoiceStatus(Invoice.InvoiceStatus.PARTIALLY_PAID);
        } else {
            invoice.setInvoiceStatus(Invoice.InvoiceStatus.UNPAID);
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