package com.compulin.rentflow.service.module3;

import com.compulin.rentflow.entity.module3.Charge;
import com.compulin.rentflow.entity.module3.Invoice;
import com.compulin.rentflow.repository.module3.ChargeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChargeService {

    private final ChargeRepository chargeRepository;
    private final InvoiceService invoiceService;

    @Autowired
    public ChargeService(ChargeRepository chargeRepository, InvoiceService invoiceService) {
        this.chargeRepository = chargeRepository;
        this.invoiceService = invoiceService;
    }


     //Get all charges associated with an invoice

    public List<Charge> getChargesByInvoiceId(Integer invoiceId) {
        return chargeRepository.findByInvoice_InvoiceId(invoiceId);
    }


     //Get all charges associated with a rental

    public List<Charge> getChargesByRentalId(Integer rentalId) {
        return chargeRepository.findByRentalId(rentalId);
    }


     //Add a new additional charge to an invoice and trigger total recalculation

    @Transactional
    public Charge addChargeToInvoice(Integer invoiceId, Charge charge) {
        Invoice invoice = invoiceService.getInvoiceById(invoiceId);
        charge.setInvoice(invoice);
        charge.setRentalId(invoice.getRentalId());

        Charge savedCharge = chargeRepository.save(charge);

        // Recalculate parent invoice totals and balance due
        invoiceService.recalculateInvoiceTotals(invoiceId);

        return savedCharge;
    }


     //Delete a charge and update the parent invoice total

    @Transactional
    public void deleteCharge(Integer chargeId) {
        Charge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new RuntimeException("Charge not found with ID: " + chargeId));

        Integer invoiceId = charge.getInvoice() != null ? charge.getInvoice().getInvoiceId() : null;

        chargeRepository.delete(charge);

        if (invoiceId != null) {
            invoiceService.recalculateInvoiceTotals(invoiceId);
        }
    }
}
