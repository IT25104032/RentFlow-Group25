package com.compulin.rentflow.controller.module3;

import com.compulin.rentflow.dto.module3.FinancialReport;
import com.compulin.rentflow.dto.module3.RentalFinancialSummary;
import com.compulin.rentflow.service.module3.FinancialReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

// View Financial Summary & Reports
@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final FinancialReportService reportService;

    public ReportController(FinancialReportService reportService) {
        this.reportService = reportService;
    }

    // GET /api/v1/reports/rental/6
    @GetMapping("/rental/{rentalId}")
    public ResponseEntity<RentalFinancialSummary> getRentalSummary(@PathVariable Integer rentalId) {
        return ResponseEntity.ok(reportService.getRentalSummary(rentalId));
    }

    // GET /api/v1/reports?type=OUTSTANDING&from=2026-10-01&to=2026-10-31
    @GetMapping
    public ResponseEntity<FinancialReport> getReport(
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer companyId) {
        return ResponseEntity.ok(reportService.getReport(type, from, to, companyId));
    }
}
