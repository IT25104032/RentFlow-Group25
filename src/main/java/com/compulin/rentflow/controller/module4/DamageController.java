package com.compulin.rentflow.controller.module4;

import com.compulin.rentflow.dto.module4.ReturnDtos.ChargeDamageRequest;
import com.compulin.rentflow.dto.module4.ReturnDtos.DamageView;
import com.compulin.rentflow.service.module4.DamageService;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * MODULE 4 (IT25104066) - damage records.
 * Damage is created while processing a return; these endpoints follow it up.
 *
 *   GET  /api/damages?status=&rentalId=&returnId=
 *   GET  /api/damages/{id}
 *   POST /api/damages/{id}/charge     { "amount": 2500 }
 *   POST /api/damages/{id}/waive
 *   POST /api/damages/{id}/repaired
 */
@RestController
@RequestMapping("/api/damages")
public class DamageController {

    private final DamageService damageService;

    public DamageController(DamageService damageService) {
        this.damageService = damageService;
    }

    @GetMapping
    public List<DamageView> list(@RequestParam(required = false) String status,
                                 @RequestParam(required = false) Integer rentalId,
                                 @RequestParam(required = false) Integer returnId,
                                 HttpSession session) {
        return damageService.list(Module4Session.companyId(session), status, returnId, rentalId);
    }

    @GetMapping("/{damageId}")
    public DamageView get(@PathVariable Integer damageId, HttpSession session) {
        return damageService.get(Module4Session.companyId(session), damageId);
    }

    @PostMapping("/{damageId}/charge")
    public DamageView charge(@PathVariable Integer damageId,
                             @RequestBody ChargeDamageRequest request,
                             HttpSession session) {
        return damageService.charge(Module4Session.companyId(session), damageId, request.amount(),
                Module4Session.userId(session, request.userId()));
    }

    @PostMapping("/{damageId}/waive")
    public DamageView waive(@PathVariable Integer damageId, HttpSession session) {
        return damageService.waive(Module4Session.companyId(session), damageId);
    }

    @PostMapping("/{damageId}/repaired")
    public DamageView repaired(@PathVariable Integer damageId, HttpSession session) {
        return damageService.markRepaired(Module4Session.companyId(session), damageId);
    }
}
