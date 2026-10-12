package com.compulin.rentflow.service.module3;

import com.compulin.rentflow.dto.module3.FinancialReport;
import com.compulin.rentflow.dto.module3.FinancialReport.InvoiceRow;
import com.compulin.rentflow.dto.module3.FinancialReport.MethodTotal;
import com.compulin.rentflow.dto.module3.FinancialReport.PaymentRow;
import com.compulin.rentflow.dto.module3.RentalFinancialSummary;
import com.compulin.rentflow.entity.module3.Invoice;
import com.compulin.rentflow.entity.module3.SecurityDeposit;
import com.compulin.rentflow.exception.module3.ReportException;
import com.compulin.rentflow.exception.module3.ResourceNotFoundException;
import com.compulin.rentflow.repository.module3.FinancialReportRepository;
import com.compulin.rentflow.repository.module3.FinancialReportRepository.RentalHeader;
import com.compulin.rentflow.repository.module3.InvoiceRepository;
import com.compulin.rentflow.repository.module3.SecurityDepositRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

// View Financial Summary & Reports
@Service
public class FinancialReportService {

    public enum ReportType {
        OUTSTANDING("Outstanding Invoices"),
        OVERDUE("Overdue Invoices"),
        PAYMENTS_BY_METHOD("Payments by Method"),
        REVENUE("Revenue for Period");

        private final String title;

        ReportType(String title) {
            this.title = title;
        }

        public String title() {
            return title;
        }
    }

    private static final List<String> OPEN_STATUSES = List.of("UNPAID", "PARTIALLY_PAID");
    private static final List<String> BILLED_STATUSES = List.of("UNPAID", "PARTIALLY_PAID", "PAID");

    private final FinancialReportRepository reportRepository;
    private final InvoiceRepository invoiceRepository;
    private final SecurityDepositRepository securityDepositRepository;

    public FinancialReportService(FinancialReportRepository reportRepository,
                                  InvoiceRepository invoiceRepository,
                                  SecurityDepositRepository securityDepositRepository) {
        this.reportRepository = reportRepository;
        this.invoiceRepository = invoiceRepository;
        this.securityDepositRepository = securityDepositRepository;
    }

    // Financial summary of one rental
    public RentalFinancialSummary getRentalSummary(Integer rentalId) {
        RentalHeader rental = reportRepository.findRentalHeader(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental #" + rentalId + " does not exist."));

        // A1: no records found
        Invoice invoice = invoiceRepository.findByRentalId(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No invoice has been generated for Rental #" + rentalId + " yet."));

        RentalFinancialSummary.DepositInfo deposit = securityDepositRepository.findByRentalId(rentalId)
                .map(this::toDepositInfo)
                .orElse(null);

        return new RentalFinancialSummary(
                rental.rentalId(), rental.customerName(), rental.customerPhone(),
                rental.startDate(), rental.dueDate(), rental.rentalStatus(),
                invoice.getInvoiceId(), invoice.getInvoiceDate(), invoice.getDueDate(),
                invoice.getInvoiceStatus().name(),
                orZero(invoice.getSubtotal()),
                orZero(invoice.getAdditionalCharges()),
                invoice.getDepositDeduction(),
                orZero(invoice.getTotalAmount()),
                orZero(invoice.getAmountPaid()),
                orZero(invoice.getBalanceDue()),
                deposit,
                reportRepository.findChargesForInvoice(invoice.getInvoiceId()),
                reportRepository.findPaymentsForInvoice(invoice.getInvoiceId()));
    }

    // Company-wide billing, payment and revenue reports
    public FinancialReport getReport(String type, LocalDate from, LocalDate to, Integer companyId) {
        ReportType reportType = parseType(type);
        if (from != null && to != null && from.isAfter(to)) {
            throw new ReportException("The 'from' date must be on or before the 'to' date.");
        }

        if (reportType == ReportType.PAYMENTS_BY_METHOD) {
            List<PaymentRow> payments = reportRepository.findCompletedPayments(from, to, companyId);
            BigDecimal totalPaid = sum(payments, PaymentRow::amount);
            return new FinancialReport(reportType.name(), reportType.title(), from, to,
                    List.of(), payments, groupByMethod(payments), payments.size(),
                    null, totalPaid, null);
        }

        List<InvoiceRow> invoices = switch (reportType) {
            case OUTSTANDING -> reportRepository.findInvoices(OPEN_STATUSES, false, from, to, companyId);
            case OVERDUE -> reportRepository.findInvoices(OPEN_STATUSES, true, from, to, companyId);
            default -> reportRepository.findInvoices(BILLED_STATUSES, false, from, to, companyId);
        };

        return new FinancialReport(reportType.name(), reportType.title(), from, to,
                invoices, List.of(), List.of(), invoices.size(),
                sum(invoices, InvoiceRow::totalAmount),
                sum(invoices, InvoiceRow::amountPaid),
                sum(invoices, InvoiceRow::balanceDue));
    }

    private ReportType parseType(String type) {
        if (type == null || type.isBlank()) {
            throw new ReportException("Please choose a report type.");
        }
        try {
            return ReportType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ReportException("Unknown report type: " + type
                    + ". Use OUTSTANDING, OVERDUE, PAYMENTS_BY_METHOD or REVENUE.");
        }
    }

    private List<MethodTotal> groupByMethod(List<PaymentRow> payments) {
        Map<String, List<PaymentRow>> byMethod = new LinkedHashMap<>();
        for (String method : List.of("CASH", "CARD", "BANK_TRANSFER")) {
            byMethod.put(method, new ArrayList<>());
        }
        for (PaymentRow payment : payments) {
            byMethod.computeIfAbsent(payment.paymentMethod(), k -> new ArrayList<>()).add(payment);
        }

        List<MethodTotal> totals = new ArrayList<>();
        byMethod.forEach((method, rows) ->
                totals.add(new MethodTotal(method, rows.size(), sum(rows, PaymentRow::amount))));
        return totals;
    }

    private RentalFinancialSummary.DepositInfo toDepositInfo(SecurityDeposit d) {
        return new RentalFinancialSummary.DepositInfo(
                orZero(d.getCalculatedDeposit()),
                orZero(d.getDepositAmountReceived()),
                orZero(d.getAmountDeducted()),
                orZero(d.getAmountRefunded()),
                d.getDepositStatus().name());
    }

    private static <T> BigDecimal sum(List<T> rows, Function<T, BigDecimal> field) {
        return rows.stream().map(field).map(FinancialReportService::orZero).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}

