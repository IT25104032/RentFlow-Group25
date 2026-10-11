package com.compulin.rentflow.service.module4;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.compulin.rentflow.service.module4.Module4Support.money;

/*
 * Module 4 tables must connect with equipment, charge, invoice, payment, security_deposit
 * Only Module 4's code is used, so SQL code is used.
 * that follows the same rules as Module 1 and Module 3:
 *   - a charge goes on the rental's open invoice and the invoice is re-totalled
 *   - invoice total = RENTAL charges (subtotal) + every other charge
 *   - a payment can't be more than the balance due
 *   - equipment quantity never goes above total_quantity
 */
@Component
public class Module4Links {

    public record InvoiceRow(Integer invoiceId, String status, BigDecimal totalAmount,
                             BigDecimal amountPaid, BigDecimal balanceDue) {
    }

    public record DepositRow(Integer depositId, String status, BigDecimal calculatedDeposit,
                             BigDecimal amountReceived, BigDecimal amountDeducted, BigDecimal amountRefunded) {
    }

    private final JdbcTemplate jdbc;

    public Module4Links(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Module 1 - Equipment
    /** Returned / repaired units are available again */
    public void addToAvailableStock(Integer equipmentId, int quantity) {
        if (equipmentId == null || quantity <= 0) {
            return;
        }
        jdbc.update("""
                UPDATE equipment
                SET available_quantity = LEAST(total_quantity, available_quantity + ?)
                WHERE equipment_id = ?
                """, quantity, equipmentId);
    }

    /** Lost units leave the company's stock. */
    public void writeOffStock(Integer equipmentId, int quantity) {
        requireEquipment(equipmentId);
        jdbc.update("""
                UPDATE equipment
                SET total_quantity = GREATEST(0, total_quantity - ?),
                    available_quantity = LEAST(available_quantity, total_quantity)
                WHERE equipment_id = ?
                """, quantity, equipmentId);
    }

    /** Lost units were found and returned. */
    public void restoreStock(Integer equipmentId, int quantity) {
        requireEquipment(equipmentId);
        jdbc.update("""
                UPDATE equipment
                SET total_quantity = total_quantity + ?,
                    available_quantity = available_quantity + ?
                WHERE equipment_id = ?
                """, quantity, quantity, equipmentId);
    }

    public Integer equipmentOfRentalItem(Integer rentalItemId) {
        List<Integer> ids = jdbc.queryForList(
                "SELECT equipment_id FROM rental_item WHERE rental_item_id = ?", Integer.class, rentalItemId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private void requireEquipment(Integer equipmentId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM equipment WHERE equipment_id = ?", Integer.class, equipmentId);
        if (n == null || n == 0) {
            throw new IllegalArgumentException("Equipment not found.");
        }
    }

    // Module 3 - charges and invoice
    /** Same as ChargeService.addCharge: saved on the open invoice (if any), then re-totalled. */
    public void addCharge(Integer rentalId, Integer rentalItemId, String type,
                          String description, BigDecimal amount, Integer userId) {
        if (amount == null || amount.signum() <= 0) {
            return;
        }
        InvoiceRow invoice = openInvoice(rentalId);
        jdbc.update("""
                INSERT INTO charge (rental_id, rental_item_id, invoice_id, charge_type,
                                    charge_description, amount, charge_date, created_by)
                VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)
                """, rentalId, rentalItemId, invoice == null ? null : invoice.invoiceId(),
                type, description, money(amount), userId);
        if (invoice != null) {
            recalculateInvoice(invoice.invoiceId());
        }
    }

    public BigDecimal removeCharges(Integer rentalId, String type, String label) {
        var rows = jdbc.queryForList("""
                SELECT charge_id, invoice_id, amount FROM charge
                WHERE rental_id = ? AND charge_type = ? AND charge_description LIKE ?
                """, rentalId, type, label + ":%");
        BigDecimal removed = BigDecimal.ZERO;
        Set<Integer> invoices = new HashSet<>();
        for (var row : rows) {
            if (row.get("invoice_id") != null) {
                invoices.add(((Number) row.get("invoice_id")).intValue());
            }
            removed = removed.add((BigDecimal) row.get("amount"));
            jdbc.update("DELETE FROM charge WHERE charge_id = ?", row.get("charge_id"));
        }
        invoices.forEach(this::recalculateInvoice);
        return removed;
    }

    public boolean hasCharges(Integer rentalId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM charge WHERE rental_id = ?", Integer.class, rentalId);
        return n != null && n > 0;
    }

    /** The rental's invoice in any status, or null. */
    public InvoiceRow invoice(Integer rentalId) {
        List<InvoiceRow> rows = jdbc.query("""
                SELECT invoice_id, invoice_status, total_amount, amount_paid, balance_due
                FROM invoice WHERE rental_id = ? ORDER BY invoice_id LIMIT 1
                """, (rs, i) -> new InvoiceRow(rs.getInt("invoice_id"), rs.getString("invoice_status"),
                money(rs.getBigDecimal("total_amount")), money(rs.getBigDecimal("amount_paid")),
                money(rs.getBigDecimal("balance_due"))), rentalId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** The rental's invoice unless it was cancelled. */
    public InvoiceRow openInvoice(Integer rentalId) {
        InvoiceRow invoice = invoice(rentalId);
        return invoice == null || "CANCELLED".equals(invoice.status()) ? null : invoice;
    }

    /** Same as InvoiceService.generateInvoiceForRental */
    public InvoiceRow createInvoice(Integer rentalId) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO invoice (rental_id, invoice_date, due_date, subtotal, additional_charges,
                                         total_amount, amount_paid, balance_due, invoice_status)
                    VALUES (?, ?, ?, 0, 0, 0, 0, 0, 'UNPAID')
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, rentalId);
            ps.setObject(2, LocalDate.now());
            ps.setObject(3, LocalDate.now().plusDays(14));
            return ps;
        }, key);
        int invoiceId = key.getKey().intValue();
        attachUnbilledCharges(rentalId, invoiceId);
        return invoice(rentalId);
    }

    /** Puts charges that were saved before the invoice existed on it, then re-totals. */
    public InvoiceRow attachUnbilledCharges(Integer rentalId, Integer invoiceId) {
        jdbc.update("UPDATE charge SET invoice_id = ? WHERE rental_id = ? AND invoice_id IS NULL",
                invoiceId, rentalId);
        recalculateInvoice(invoiceId);
        return invoice(rentalId);
    }

    /** Same as InvoiceService.recalculateInvoiceTotals. */
    public void recalculateInvoice(Integer invoiceId) {
        jdbc.update("""
                UPDATE invoice i
                JOIN (SELECT COALESCE(SUM(CASE WHEN charge_type = 'RENTAL' THEN amount END), 0) AS sub,
                             COALESCE(SUM(CASE WHEN charge_type <> 'RENTAL' THEN amount END), 0) AS extra
                      FROM charge WHERE invoice_id = ?) c
                SET i.subtotal = c.sub,
                    i.additional_charges = c.extra,
                    i.total_amount = c.sub + c.extra,
                    i.balance_due = c.sub + c.extra - COALESCE(i.amount_paid, 0),
                    i.invoice_status = CASE
                        WHEN i.invoice_status = 'CANCELLED' THEN 'CANCELLED'
                        WHEN COALESCE(i.amount_paid, 0) > 0 AND c.sub + c.extra - i.amount_paid <= 0 THEN 'PAID'
                        WHEN COALESCE(i.amount_paid, 0) > 0 THEN 'PARTIALLY_PAID'
                        ELSE 'UNPAID' END
                WHERE i.invoice_id = ?
                """, invoiceId, invoiceId);
    }

    /** Same as PaymentService.recordPayment. */
    public void recordPayment(Integer invoiceId, BigDecimal amount, String method, String referenceNo, Integer userId) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
        BigDecimal balance = jdbc.queryForObject(
                "SELECT balance_due FROM invoice WHERE invoice_id = ?", BigDecimal.class, invoiceId);
        if (amount.compareTo(money(balance)) > 0) {
            throw new IllegalArgumentException("Payment exceeds remaining balance. Due: " + money(balance));
        }
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO payment (invoice_id, amount, payment_date, payment_method,
                                         reference_no, received_by, payment_status)
                    VALUES (?, ?, NOW(), ?, ?, ?, 'COMPLETED')
                    """);
            ps.setInt(1, invoiceId);
            ps.setBigDecimal(2, money(amount));
            ps.setString(3, method);
            ps.setString(4, referenceNo != null ? referenceNo : "TXN-" + System.currentTimeMillis());
            if (userId == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, userId);
            }
            return ps;
        });
        jdbc.update("""
                UPDATE invoice
                SET amount_paid = amount_paid + ?,
                    balance_due = total_amount - amount_paid,
                    invoice_status = IF(total_amount - amount_paid = 0, 'PAID', 'PARTIALLY_PAID')
                WHERE invoice_id = ?
                """, money(amount), invoiceId);
    }

    // Module 3 - security deposit
    public DepositRow deposit(Integer rentalId) {
        List<DepositRow> rows = jdbc.query("""
                SELECT deposit_id, deposit_status, calculated_deposit, deposit_amount_received,
                       amount_deducted, amount_refunded
                FROM security_deposit WHERE rental_id = ?
                """, (rs, i) -> new DepositRow(rs.getInt("deposit_id"), rs.getString("deposit_status"),
                money(rs.getBigDecimal("calculated_deposit")), money(rs.getBigDecimal("deposit_amount_received")),
                money(rs.getBigDecimal("amount_deducted")), money(rs.getBigDecimal("amount_refunded"))), rentalId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void closeDeposit(Integer depositId, BigDecimal deducted, BigDecimal refunded, String status) {
        jdbc.update("""
                UPDATE security_deposit
                SET amount_deducted = ?, amount_refunded = ?, refund_date = NOW(), deposit_status = ?
                WHERE deposit_id = ?
                """, money(deducted), money(refunded), status, depositId);
    }
}