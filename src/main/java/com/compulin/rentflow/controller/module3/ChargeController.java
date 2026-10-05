package com.compulin.rentflow.controller.module3;

import com.compulin.rentflow.entity.module3.Charge;
import com.compulin.rentflow.repository.module3.ChargeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<List<Map<String, Object>>> getChargesByInvoiceId(@PathVariable Integer invoiceId) {
        List<Charge> charges = chargeRepository.findByInvoice_InvoiceId(invoiceId);

        // Manually map only the exact fields React needs, stripping away all relationships and dates
        List<Map<String, Object>> safeCharges = charges.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("chargeType", c.getChargeType().name());
            map.put("chargeDescription", c.getChargeDescription());
            map.put("amount", c.getAmount());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(safeCharges);
    }

    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<List<Charge>> getChargesByRentalId(@PathVariable Integer rentalId) {
        List<Charge> charges = chargeRepository.findByRentalId(rentalId);
        return ResponseEntity.ok(charges);
    }
}