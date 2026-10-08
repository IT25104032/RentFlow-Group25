package com.compulin.rentflow.controller.module4;

import com.compulin.rentflow.dto.module4.ReturnDtos.ChargeDamageRequest;
import com.compulin.rentflow.dto.module4.ReturnDtos.LostItemRequest;
import com.compulin.rentflow.dto.module4.ReturnDtos.LostLine;
import com.compulin.rentflow.dto.module4.ReturnDtos.LostView;
import com.compulin.rentflow.service.module4.LostItemService;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * MODULE 4 (IT25104066) - lost notes.
 *
 *   GET  /api/lost-items?status=&rentalId=
 *   POST /api/lost-items                 record units that did not come back
 *   POST /api/lost-items/{id}/charge     { "amount": 75000 }  (replacement cost per unit)
 *   POST /api/lost-items/{id}/recover    the units were found and handed back
 */
@RestController
@RequestMapping("/api/lost-items")
public class LostItemController {

    private final LostItemService lostItemService;

    public LostItemController(LostItemService lostItemService) {
        this.lostItemService = lostItemService;
    }

    @GetMapping
    public List<LostView> list(@RequestParam(required = false) String status,
                               @RequestParam(required = false) Integer rentalId,
                               HttpSession session) {
        return lostItemService.list(Module4Session.companyId(session), status, rentalId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LostView record(@RequestBody LostItemRequest request, HttpSession session) {
        Integer userId = Module4Session.userId(session, request.reportedBy());
        if (userId == null) {
            throw new IllegalArgumentException("Please log in before recording a lost item.");
        }
        LostLine line = new LostLine(request.rentalItemId(), request.quantityLost(), request.lossType(),
                request.replacementCostPerUnit(), request.reason(), request.notes());
        return lostItemService.recordLost(Module4Session.companyId(session), request.rentalId(), line, userId);
    }

    @PostMapping("/{lostItemId}/charge")
    public LostView charge(@PathVariable Integer lostItemId,
                           @RequestBody ChargeDamageRequest request,
                           HttpSession session) {
        return lostItemService.charge(Module4Session.companyId(session), lostItemId, request.amount(),
                Module4Session.userId(session, request.userId()));
    }

    @PostMapping("/{lostItemId}/recover")
    public LostView recover(@PathVariable Integer lostItemId, HttpSession session) {
        return lostItemService.recover(Module4Session.companyId(session), lostItemId);
    }
}
