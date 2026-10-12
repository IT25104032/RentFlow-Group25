package com.compulin.rentflow.controller.module3;

import com.compulin.rentflow.entity.module3.Invoice;
import com.compulin.rentflow.service.module3.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.compulin.rentflow.dto.module3.InvoicePreview;
import com.compulin.rentflow.dto.module3.ExtensionPreview;

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


     // Generates a new invoice for a given rental ID by collecting rental items and charges

    @PostMapping("/generate/{rentalId}")
    public ResponseEntity<Invoice> generateInvoice(@PathVariable Integer rentalId) {
        Invoice createdInvoice = invoiceService.generateInvoiceForRental(rentalId);
        return new ResponseEntity<>(createdInvoice, HttpStatus.CREATED);
    }


     //Retrieves an invoice by its primary key ID

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Integer id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }


     //Retrieves the invoice associated with a specific rental ID

    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<Invoice> getInvoiceByRentalId(@PathVariable Integer rentalId) {
        Invoice invoice = invoiceService.getInvoiceByRentalId(rentalId);
        return ResponseEntity.ok(invoice);
    }


     //Retrieves all invoices in the system

    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }


     //Recalculates invoice totals when late fees or damages are added

    @PostMapping("/{id}/recalculate")
    public ResponseEntity<Invoice> recalculateInvoice(@PathVariable Integer id) {
        Invoice updatedInvoice = invoiceService.recalculateInvoiceTotals(id);
        return ResponseEntity.ok(updatedInvoice);
    }


     //Cancels an invoice

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Invoice> cancelInvoice(@PathVariable Integer id) {
        Invoice cancelledInvoice = invoiceService.cancelInvoice(id);
        return ResponseEntity.ok(cancelledInvoice);
    }

    // Shows the invoice breakdown for review before it is generated
    @GetMapping("/preview/{rentalId}")
    public ResponseEntity<InvoicePreview> previewInvoice(@PathVariable Integer rentalId) {
        return ResponseEntity.ok(invoiceService.previewInvoice(rentalId));
    }

    // Shows previous vs updated amounts after the rental is extended
    @GetMapping("/{id}/extension-preview")
    public ResponseEntity<ExtensionPreview> previewExtension(@PathVariable Integer id) {
        return ResponseEntity.ok(invoiceService.previewExtensionUpdate(id));
    }

    // Saves the recalculated charges for the extended rental period
    @PutMapping("/{id}/apply-extension")
    public ResponseEntity<Invoice> applyExtension(@PathVariable Integer id) {
        return ResponseEntity.ok(invoiceService.applyExtensionUpdate(id));
    }
}
