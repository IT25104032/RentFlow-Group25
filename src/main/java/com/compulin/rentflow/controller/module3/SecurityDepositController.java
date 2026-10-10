package com.compulin.rentflow.controller.module3;

import com.compulin.rentflow.entity.module3.SecurityDeposit;
import com.compulin.rentflow.service.module3.SecurityDepositService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/deposits")
@CrossOrigin(origins = "*")
public class SecurityDepositController {

    private final SecurityDepositService securityDepositService;

    public SecurityDepositController(SecurityDepositService securityDepositService) {
        this.securityDepositService = securityDepositService;
    }

    // Retrieves all security deposits
    @GetMapping
    public ResponseEntity<List<SecurityDeposit>> getAllDeposits() {
        return ResponseEntity.ok(securityDepositService.getAllDeposits());
    }

    // Retrieves the deposit for a specific rental
    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<SecurityDeposit> getDepositByRentalId(@PathVariable Integer rentalId) {
        return ResponseEntity.ok(securityDepositService.getDepositByRentalId(rentalId));
    }

    // Creates a PENDING deposit record for a rental
    // Body: { "rentalId": 4, "calculatedDeposit": 6000 }
    @PostMapping
    public ResponseEntity<SecurityDeposit> initializeDeposit(@RequestBody Map<String, Object> body) {
        Integer rentalId = Integer.valueOf(body.get("rentalId").toString());
        BigDecimal calculatedDeposit = new BigDecimal(body.get("calculatedDeposit").toString());
        return new ResponseEntity<>(securityDepositService.initializeDeposit(rentalId, calculatedDeposit), HttpStatus.CREATED);
    }

    // Records the deposit being collected from the customer (PENDING -> HELD)
    // Body: { "amountReceived": 6000 }
    @PutMapping("/{id}/receive")
    public ResponseEntity<SecurityDeposit> receiveDeposit(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        BigDecimal amountReceived = new BigDecimal(body.get("amountReceived").toString());
        // receivedBy is null until login is integrated
        return ResponseEntity.ok(securityDepositService.receiveDeposit(id, amountReceived, null));
    }

    // Refunds the deposit after return, minus any deduction (HELD -> REFUNDED / PARTIALLY_REFUNDED / FORFEITED)
    // Body: { "amountDeducted": 2000 }
    @PutMapping("/{id}/refund")
    public ResponseEntity<SecurityDeposit> processDepositReturn(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        BigDecimal amountDeducted = new BigDecimal(body.getOrDefault("amountDeducted", "0").toString());
        return ResponseEntity.ok(securityDepositService.processDepositReturn(id, amountDeducted));
    }
}

