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


}
