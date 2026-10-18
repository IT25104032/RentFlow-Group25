package com.compulin.rentflow.controller.module3;

import com.compulin.rentflow.entity.module3.SecurityDeposit;
import com.compulin.rentflow.service.module3.SecurityDepositService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import com.compulin.rentflow.security.Module3Access;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/deposits")
public class SecurityDepositController {

    private final SecurityDepositService securityDepositService;
    private final Module3Access access;

    public SecurityDepositController(SecurityDepositService securityDepositService, Module3Access access) {
        this.securityDepositService = securityDepositService;
        this.access = access;
    }

    // Retrieves all security deposits
    @GetMapping
    public ResponseEntity<List<SecurityDeposit>> getAllDeposits(HttpSession session) {
        access.companyId(session);
        return ResponseEntity.ok(securityDepositService.getAllDeposits().stream()
                .filter(deposit -> {
                    try { access.rental(deposit.getRentalId(), session); return true; }
                    catch (org.springframework.web.server.ResponseStatusException ex) { return false; }
                }).toList());
    }

    // Retrieves the deposit for a specific rental
    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<SecurityDeposit> getDepositByRentalId(@PathVariable Integer rentalId, HttpSession session) {
        access.rental(rentalId, session);
        return ResponseEntity.ok(securityDepositService.getDepositByRentalId(rentalId));
    }

    // Records the deposit being collected from the customer (PENDING -> HELD)
    // Body: { "amountReceived": 6000 }
    @PutMapping("/{id}/receive")
    public ResponseEntity<SecurityDeposit> receiveDeposit(@PathVariable Integer id, @RequestBody Map<String, Object> body, HttpSession session) {
        access.deposit(id, session);
        BigDecimal amountReceived = new BigDecimal(body.get("amountReceived").toString());
        // receivedBy is null until login is integrated
        return ResponseEntity.ok(securityDepositService.receiveDeposit(id, amountReceived, access.userId(session)));
    }

    // Refunds the deposit after return, minus any deduction
    @PutMapping("/{id}/refund")
    public ResponseEntity<SecurityDeposit> processDepositReturn(@PathVariable Integer id, @RequestBody Map<String, Object> body, HttpSession session) {
        access.deposit(id, session);
        BigDecimal amountDeducted = new BigDecimal(body.getOrDefault("amountDeducted", "0").toString());
        return ResponseEntity.ok(securityDepositService.processDepositReturn(id, amountDeducted));
    }
}
