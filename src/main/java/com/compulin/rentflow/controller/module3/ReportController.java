package com.compulin.rentflow.controller.module3;

import com.compulin.rentflow.dto.module3.FinancialReport;
import com.compulin.rentflow.dto.module3.RentalFinancialSummary;
import com.compulin.rentflow.service.module3.FinancialReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import com.compulin.rentflow.security.Module3Access;

import java.time.LocalDate;

// View Financial Summary & Reports
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final FinancialReportService reportService;
    private final Module3Access access;

    public ReportController(FinancialReportService reportService, Module3Access access) {
        this.reportService = reportService;
        this.access = access;
    }

    // GET /api/v1/reports/rental/6
    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<RentalFinancialSummary> getRentalSummary(@PathVariable Integer rentalId, HttpSession session) {
        access.rental(rentalId, session);
        return ResponseEntity.ok(reportService.getRentalSummary(rentalId));
    }

    // GET /api/v1/reports?type=OUTSTANDING&from=2026-10-01&to=2026-10-31
    @GetMapping
    public ResponseEntity<FinancialReport> getReport(
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer companyId, HttpSession session) {
        Integer authenticatedCompany = access.companyId(session);
        if (companyId != null && !companyId.equals(authenticatedCompany)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Company mismatch");
        }
        return ResponseEntity.ok(reportService.getReport(type, from, to, authenticatedCompany));
    }
}
