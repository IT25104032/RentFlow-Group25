package com.compulin.rentflow.controller.module3;

import com.compulin.rentflow.entity.module3.Charge;
import com.compulin.rentflow.repository.module3.ChargeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/charges")
@CrossOrigin(origins = "*")
public class ChargeController {

    private final ChargeRepository chargeRepository;

    @Autowired
    public ChargeController(ChargeRepository chargeRepository) {
        this.chargeRepository = chargeRepository;
    }

    // Save a new charge
    @PostMapping
    public ResponseEntity<Charge> createCharge(@RequestBody Charge charge) {
        Charge savedCharge = chargeRepository.save(charge);
        return new ResponseEntity<>(savedCharge, HttpStatus.CREATED);
    }

    // Get all charges for a specific invoice
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<Charge>> getChargesByInvoiceId(@PathVariable Integer invoiceId) {
        List<Charge> charges = chargeRepository.findByInvoice_InvoiceId(invoiceId);
        return ResponseEntity.ok(charges);
    }
}