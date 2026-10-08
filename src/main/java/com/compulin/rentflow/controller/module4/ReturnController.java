package com.compulin.rentflow.controller.module4;

import com.compulin.rentflow.dto.module4.ReturnDtos.Module4Counts;
import com.compulin.rentflow.dto.module4.ReturnDtos.OpenRental;
import com.compulin.rentflow.dto.module4.ReturnDtos.ProcessReturnRequest;
import com.compulin.rentflow.dto.module4.ReturnDtos.ProcessReturnResponse;
import com.compulin.rentflow.dto.module4.ReturnDtos.RentalForReturn;
import com.compulin.rentflow.dto.module4.ReturnDtos.ReturnDetails;
import com.compulin.rentflow.dto.module4.ReturnDtos.ReturnSummary;
import com.compulin.rentflow.service.module4.RentalReturnService;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * MODULE 4 (IT25104066) - returns.
 *
 *   GET  /api/returns/overview                  numbers for the overview / nav bar
 *   GET  /api/returns/open-rentals?search=      rentals with equipment out
 *   GET  /api/returns/rental/{id}?returnDate=   return screen for one rental
 *   POST /api/returns/process                   record a return (+ damage, lost, late)
 *   GET  /api/returns?search=&rentalId=         return history
 *   GET  /api/returns/{returnId}                one return with its items and damage
 */
@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final RentalReturnService returnService;

    public ReturnController(RentalReturnService returnService) {
        this.returnService = returnService;
    }

    @GetMapping("/overview")
    public Module4Counts overview(HttpSession session) {
        return returnService.counts(Module4Session.companyId(session));
    }

    @GetMapping("/open-rentals")
    public List<OpenRental> openRentals(@RequestParam(required = false) String search,
                                        HttpSession session) {
        return returnService.openRentals(Module4Session.companyId(session), search);
    }

    @GetMapping("/rental/{rentalId}")
    public RentalForReturn rentalForReturn(@PathVariable Integer rentalId,
                                           @RequestParam(required = false) String returnDate,
                                           HttpSession session) {
        return returnService.rentalForReturn(rentalId, Module4Session.companyId(session), returnDate);
    }

    @PostMapping("/process")
    public ProcessReturnResponse processReturn(@RequestBody ProcessReturnRequest request,
                                               HttpSession session) {
        return returnService.processReturn(
                request,
                Module4Session.userId(session, request == null ? null : request.processedBy()),
                Module4Session.companyId(session));
    }

    @GetMapping
    public List<ReturnSummary> history(@RequestParam(required = false) String search,
                                       @RequestParam(required = false) Integer rentalId,
                                       HttpSession session) {
        return returnService.history(Module4Session.companyId(session), search, rentalId);
    }

    @GetMapping("/{returnId}")
    public ReturnDetails details(@PathVariable Integer returnId, HttpSession session) {
        return returnService.details(Module4Session.companyId(session), returnId);
    }
}
