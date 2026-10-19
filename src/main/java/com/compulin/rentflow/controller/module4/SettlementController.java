package com.compulin.rentflow.controller.module4;

import com.compulin.rentflow.dto.module4.SettlementDtos.SettleRequest;
import com.compulin.rentflow.dto.module4.SettlementDtos.SettlementPaymentRequest;
import com.compulin.rentflow.dto.module4.SettlementDtos.SettlementPreview;
import com.compulin.rentflow.dto.module4.SettlementDtos.SettlementRow;
import com.compulin.rentflow.service.module4.SettlementService;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import com.compulin.rentflow.controller.AuthController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/*
 * MODULE 4 (IT25104066) - final settlement.
 */
@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    private void requireRentalOfficer(HttpSession session) {
        Object roleValue = session.getAttribute(AuthController.SESSION_ROLE);

        String role = roleValue == null ? "" : roleValue.toString();

        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        if (!"RENTAL_OFFICER".equals(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only rental officers can perform settlements and record payments."
            );
        }
    }

    @GetMapping
    public List<SettlementRow> list(@RequestParam(required = false) String search, HttpSession session) {
        return settlementService.list(Module4Session.companyId(session), search);
    }

    @GetMapping("/{rentalId}")
    public SettlementPreview preview(@PathVariable Integer rentalId, HttpSession session) {
        return settlementService.preview(Module4Session.companyId(session), rentalId);
    }

    @PostMapping("/{rentalId}")
    public SettlementPreview settle(@PathVariable Integer rentalId,
                                    @RequestBody(required = false) SettleRequest request,
                                    HttpSession session) {
        requireRentalOfficer(session);
        return settlementService.settle(Module4Session.companyId(session), rentalId,
                Module4Session.userId(session, request == null ? null : request.userId()));
    }

    @PostMapping("/{rentalId}/payments")
    public SettlementPreview pay(@PathVariable Integer rentalId,
                                 @RequestBody SettlementPaymentRequest request,
                                 HttpSession session) {
        requireRentalOfficer(session);
        return settlementService.payAndSettle(Module4Session.companyId(session), rentalId,
                request.amount(), request.paymentMethod(), request.referenceNo(),
                Module4Session.userId(session, request.userId()));
    }
}

