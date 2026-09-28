package com.compulin.rentflow.controller.module3;

import com.compulin.rentflow.entity.module3.Invoice;
import com.compulin.rentflow.service.module3.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Generates a new invoice for a given rental ID by collecting rental items and charges
     * POST /api/v1/invoices/generate/{rentalId}
     */
    @PostMapping("/generate/{rentalId}")
    public ResponseEntity<Invoice> generateInvoice(@PathVariable Integer rentalId) {
        Invoice createdInvoice = invoiceService.generateInvoiceForRental(rentalId);
        return new ResponseEntity<>(createdInvoice, HttpStatus.CREATED);
    }

    /**
     * Retrieves an invoice by its primary key ID
     * GET /api/v1/invoices/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Integer id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    /**
     * Retrieves the invoice associated with a specific rental ID
     * GET /api/v1/invoices/rental/{rentalId}
     */
    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<Invoice> getInvoiceByRentalId(@PathVariable Integer rentalId) {
        Invoice invoice = invoiceService.getInvoiceByRentalId(rentalId);
        return ResponseEntity.ok(invoice);
    }

    /**
     * Retrieves all invoices in the system
     * GET /api/v1/invoices
     */
    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    /**
     * Recalculates invoice totals when late fees or damages are added
     * POST /api/v1/invoices/{id}/recalculate
     */
    @PostMapping("/{id}/recalculate")
    public ResponseEntity<Invoice> recalculateInvoice(@PathVariable Integer id) {
        Invoice updatedInvoice = invoiceService.recalculateInvoiceTotals(id);
        return ResponseEntity.ok(updatedInvoice);
    }

    /**
     * Cancels an invoice
     * PUT /api/v1/invoices/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Invoice> cancelInvoice(@PathVariable Integer id) {
        Invoice cancelledInvoice = invoiceService.cancelInvoice(id);
        return ResponseEntity.ok(cancelledInvoice);
    }
}
