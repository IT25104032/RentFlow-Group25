package com.compulin.rentflow.repository.module3;

import com.compulin.rentflow.dto.module3.FinancialReport.InvoiceRow;
import com.compulin.rentflow.dto.module3.FinancialReport.PaymentRow;
import com.compulin.rentflow.dto.module3.RentalFinancialSummary.ChargeLine;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Read-only queries for the financial summary and reports
// Joins rental and customer of Module 2 and sys_user of Module 1 only to show names
@Repository
public class FinancialReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public FinancialReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record RentalHeader(Integer rentalId, String customerName, String customerPhone,
                               LocalDate startDate, LocalDate dueDate, String rentalStatus) {}

    public Optional<RentalHeader> findRentalHeader(Integer rentalId) {
        List<RentalHeader> rows = jdbcTemplate.query(
                "SELECT r.rental_id, r.start_date, r.due_date, r.rental_status, c.customer_name, c.phone " +
                        "FROM rental r LEFT JOIN customer c ON c.customer_id = r.customer_id WHERE r.rental_id = ?",
                (rs, i) -> new RentalHeader(
                        rs.getInt("rental_id"),
                        rs.getString("customer_name"),
                        rs.getString("phone"),
                        toDate(rs.getDate("start_date")),
                        toDate(rs.getDate("due_date")),
                        rs.getString("rental_status")),
                rentalId);
        return rows.stream().findFirst();
    }

    public List<ChargeLine> findChargesForInvoice(Integer invoiceId) {
        return jdbcTemplate.query(
                "SELECT charge_type, charge_description, amount FROM charge WHERE invoice_id = ? ORDER BY charge_id",
                (rs, i) -> new ChargeLine(rs.getString("charge_type"), rs.getString("charge_description"),
                        rs.getBigDecimal("amount")),
                invoiceId);
    }

    // Invoices filtered by status and invoice date. overdueOnly keeps those past their due date
    public List<InvoiceRow> findInvoices(List<String> statuses, boolean overdueOnly,
                                         LocalDate from, LocalDate to, Integer companyId) {
        StringBuilder sql = new StringBuilder(
                "SELECT i.invoice_id, i.rental_id, c.customer_name, i.invoice_date, i.due_date, " +
                        "i.total_amount, i.amount_paid, i.balance_due, i.invoice_status, " +
                        "CASE WHEN i.due_date < CURDATE() AND i.balance_due > 0 " +
                        "     THEN DATEDIFF(CURDATE(), i.due_date) ELSE 0 END AS days_overdue " +
                        "FROM invoice i JOIN rental r ON r.rental_id = i.rental_id " +
                        "LEFT JOIN customer c ON c.customer_id = r.customer_id " +
                        "WHERE i.invoice_status IN (" + String.join(",", statuses.stream().map(s -> "?").toList()) + ")");
        List<Object> params = new ArrayList<>(statuses);

        if (overdueOnly) {
            sql.append(" AND i.due_date < CURDATE() AND i.balance_due > 0");
        }
        if (from != null) {
            sql.append(" AND i.invoice_date >= ?");
            params.add(from);
        }
        if (to != null) {
            sql.append(" AND i.invoice_date <= ?");
            params.add(to);
        }
        if (companyId != null) {
            sql.append(" AND r.company_id = ?");
            params.add(companyId);
        }
        sql.append(" ORDER BY i.invoice_date, i.invoice_id");

        return jdbcTemplate.query(sql.toString(), (rs, i) -> new InvoiceRow(
                rs.getInt("invoice_id"),
                rs.getInt("rental_id"),
                rs.getString("customer_name"),
                toDate(rs.getDate("invoice_date")),
                toDate(rs.getDate("due_date")),
                rs.getBigDecimal("total_amount"),
                rs.getBigDecimal("amount_paid"),
                rs.getBigDecimal("balance_due"),
                rs.getString("invoice_status"),
                rs.getInt("days_overdue")), params.toArray());
    }

    // Completed payments received between from and to
    public List<PaymentRow> findCompletedPayments(LocalDate from, LocalDate to, Integer companyId) {
        StringBuilder sql = new StringBuilder(PAYMENT_SELECT + " WHERE p.payment_status = 'COMPLETED'");
        List<Object> params = new ArrayList<>();

        if (from != null) {
            sql.append(" AND p.payment_date >= ?");
            params.add(from.atStartOfDay());
        }
        if (to != null) {
            sql.append(" AND p.payment_date < ?");
            params.add(to.plusDays(1).atStartOfDay());
        }
        if (companyId != null) {
            sql.append(" AND r.company_id = ?");
            params.add(companyId);
        }
        sql.append(" ORDER BY p.payment_date, p.payment_id");

        return jdbcTemplate.query(sql.toString(), this::mapPayment, params.toArray());
    }

    // Every payment for one invoice, voided ones included, for the rental summary
    public List<PaymentRow> findPaymentsForInvoice(Integer invoiceId) {
        return jdbcTemplate.query(PAYMENT_SELECT + " WHERE p.invoice_id = ? ORDER BY p.payment_date, p.payment_id",
                this::mapPayment, invoiceId);
    }

    private static final String PAYMENT_SELECT =
            "SELECT p.payment_id, p.invoice_id, i.rental_id, c.customer_name, p.payment_date, " +
                    "p.payment_method, p.reference_no, p.amount, p.payment_status, u.full_name AS staff_name " +
                    "FROM payment p JOIN invoice i ON i.invoice_id = p.invoice_id " +
                    "JOIN rental r ON r.rental_id = i.rental_id " +
                    "LEFT JOIN customer c ON c.customer_id = r.customer_id " +
                    "LEFT JOIN sys_user u ON u.user_id = p.received_by";

    private PaymentRow mapPayment(ResultSet rs, int rowNum) throws SQLException {
        Timestamp paidAt = rs.getTimestamp("payment_date");
        return new PaymentRow(
                rs.getInt("payment_id"),
                rs.getInt("invoice_id"),
                rs.getInt("rental_id"),
                rs.getString("customer_name"),
                paidAt != null ? paidAt.toLocalDateTime() : null,
                rs.getString("payment_method"),
                rs.getString("reference_no"),
                rs.getBigDecimal("amount"),
                rs.getString("payment_status"),
                rs.getString("staff_name"));
    }

    private static LocalDate toDate(Date date) {
        return date != null ? date.toLocalDate() : null;
    }
}
