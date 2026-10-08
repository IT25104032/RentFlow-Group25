package com.compulin.rentflow.repository.module4;

import com.compulin.rentflow.dto.module4.ReturnDtos.DamageView;
import com.compulin.rentflow.dto.module4.ReturnDtos.LostView;
import com.compulin.rentflow.dto.module4.ReturnDtos.Module4Counts;
import com.compulin.rentflow.dto.module4.ReturnDtos.OpenRental;
import com.compulin.rentflow.dto.module4.ReturnDtos.ReturnSummary;
import com.compulin.rentflow.dto.module4.ReturnDtos.ReturnedItemView;
import com.compulin.rentflow.dto.module4.SettlementDtos.ChargeLine;
import com.compulin.rentflow.dto.module4.SettlementDtos.SettlementRow;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * MODULE 4 (IT25104066) - read queries that join several modules' tables
 * (rental, customer, equipment, charge, ...) and the few status updates
 * Module 4 makes on Module 2's rental tables.
 *
 * "Units out" of a rental line = issued quantity - returned - recorded lost.
 * Lines still SELECTED (never issued) are ignored.
 */
@Repository
public class RentalLookupRepository {

    private static final String RETURNED_QTY =
            "COALESCE((SELECT SUM(x.qty_returned) FROM return_item x "
                    + "WHERE x.rental_item_id = ri.rental_item_id), 0)";

    private static final String LOST_QTY =
            "COALESCE((SELECT SUM(l.quantity_lost) FROM lost_item l "
                    + "WHERE l.rental_item_id = ri.rental_item_id), 0)";

    /** Units still out for rental r (used as a column). */
    private static final String UNITS_OUT =
            "(SELECT COALESCE(SUM(ri.quantity - " + RETURNED_QTY + " - " + LOST_QTY + "), 0) "
                    + "FROM rental_item ri WHERE ri.rental_id = r.rental_id "
                    + "AND ri.item_status <> 'SELECTED')";

    private final JdbcTemplate jdbc;

    public RentalLookupRepository(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    // =========================================================
    // Rentals
    // =========================================================

    /** Header of one rental, or null if it does not exist. */
    public Map<String, Object> findRental(Integer rentalId) {
        try {
            return jdbc.queryForMap("""
                    SELECT r.rental_id, r.company_id, r.start_date, r.due_date,
                           r.rental_status, c.customer_name, c.phone,
                           """ + UNITS_OUT + """
                     AS units_out
                    FROM rental r
                    JOIN customer c ON c.customer_id = r.customer_id
                    WHERE r.rental_id = ?
                    """, rentalId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** Issued lines of a rental with returned / lost / remaining quantities. */
    public List<Map<String, Object>> findIssuedItems(Integer rentalId) {
        return jdbc.queryForList("""
                SELECT ri.rental_item_id, ri.equipment_id, e.item_name, e.item_code,
                       ri.rate_per_unit, ri.rate_period, ri.quantity, ri.item_status,
                       """ + RETURNED_QTY + " AS returned_qty, " + LOST_QTY + """
                 AS lost_qty
                FROM rental_item ri
                JOIN equipment e ON e.equipment_id = ri.equipment_id
                WHERE ri.rental_id = ? AND ri.item_status <> 'SELECTED'
                ORDER BY ri.rental_item_id
                """, rentalId);
    }

    /** Rentals with equipment still out (Process Return step 1). */
    public List<OpenRental> findOpenRentals(Integer companyId, String search) {
        List<Object> args = new ArrayList<>();
        String where = companyFilter("r", companyId, args) + searchFilter(search, args);

        return jdbc.query("""
                SELECT * FROM (
                    SELECT r.rental_id, c.customer_name, c.phone, r.start_date, r.due_date,
                           r.rental_status, GREATEST(DATEDIFF(CURDATE(), r.due_date), 0) AS days_overdue,
                           """ + UNITS_OUT + """
                     AS units_out
                    FROM rental r
                    JOIN customer c ON c.customer_id = r.customer_id
                    WHERE r.rental_status IN ('ACTIVE', 'OVERDUE', 'PARTIALLY_RETURNED')
                    """ + where + """
                ) t
                WHERE t.units_out > 0
                ORDER BY t.due_date, t.rental_id
                """, (rs, i) -> new OpenRental(
                rs.getInt("rental_id"),
                rs.getString("customer_name"),
                rs.getString("phone"),
                rs.getString("start_date"),
                rs.getString("due_date"),
                rs.getString("rental_status"),
                rs.getInt("units_out"),
                rs.getLong("days_overdue")), args.toArray());
    }

    public void updateRentalItemStatus(Integer rentalItemId, String status) {
        jdbc.update("UPDATE rental_item SET item_status = ? WHERE rental_item_id = ?",
                status, rentalItemId);
    }

    public void updateRentalStatus(Integer rentalId, String status) {
        jdbc.update("UPDATE rental SET rental_status = ? WHERE rental_id = ?",
                status, rentalId);
    }

    /** On close: every line that is not LOST becomes CLOSED. */
    public void closeRentalItems(Integer rentalId) {
        jdbc.update("""
                UPDATE rental_item SET item_status = 'CLOSED'
                WHERE rental_id = ? AND item_status NOT IN ('LOST', 'SELECTED')
                """, rentalId);
    }

    // =========================================================
    // Return history
    // =========================================================

    public List<ReturnSummary> findReturns(Integer companyId, String search, Integer rentalId, Integer returnId) {
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1 = 1")
                .append(companyFilter("r", companyId, args))
                .append(searchFilter(search, args));
        if (rentalId != null) {
            where.append(" AND rr.rental_id = ?");
            args.add(rentalId);
        }
        if (returnId != null) {
            where.append(" AND rr.return_id = ?");
            args.add(returnId);
        }

        return jdbc.query("""
                SELECT rr.return_id, rr.rental_id, c.customer_name, rr.return_date,
                       rr.return_type, u.full_name AS processed_by_name, rr.notes,
                       (SELECT COALESCE(SUM(x.qty_returned), 0) FROM return_item x
                         WHERE x.return_id = rr.return_id) AS total_units,
                       (SELECT COALESCE(SUM(d.damaged_quantity), 0) FROM damage_record d
                          JOIN return_item x ON x.return_item_id = d.return_item_id
                         WHERE x.return_id = rr.return_id) AS damaged_units
                FROM rental_return rr
                JOIN rental r ON r.rental_id = rr.rental_id
                JOIN customer c ON c.customer_id = r.customer_id
                LEFT JOIN sys_user u ON u.user_id = rr.processed_by
                """ + where + """
                 ORDER BY rr.return_date DESC, rr.return_id DESC
                """, (rs, i) -> new ReturnSummary(
                rs.getInt("return_id"),
                rs.getInt("rental_id"),
                rs.getString("customer_name"),
                rs.getString("return_date"),
                rs.getString("return_type"),
                rs.getString("processed_by_name"),
                rs.getInt("total_units"),
                rs.getInt("damaged_units"),
                rs.getString("notes")), args.toArray());
    }

    public List<ReturnedItemView> findReturnedItems(Integer returnId) {
        return jdbc.query("""
                SELECT x.return_item_id, x.rental_item_id, e.item_name, e.item_code,
                       x.qty_returned, x.condition_status, x.inspection_notes, x.returned_at
                FROM return_item x
                JOIN rental_item ri ON ri.rental_item_id = x.rental_item_id
                JOIN equipment e ON e.equipment_id = ri.equipment_id
                WHERE x.return_id = ?
                ORDER BY x.return_item_id
                """, (rs, i) -> new ReturnedItemView(
                rs.getInt("return_item_id"),
                rs.getInt("rental_item_id"),
                rs.getString("item_name"),
                rs.getString("item_code"),
                rs.getInt("qty_returned"),
                rs.getString("condition_status"),
                rs.getString("inspection_notes"),
                rs.getString("returned_at")), returnId);
    }

    // =========================================================
    // Damage records and lost items
    // =========================================================

    public List<DamageView> findDamages(Integer companyId, String status, Integer returnId,
                                        Integer rentalId, Integer damageId) {
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1 = 1").append(companyFilter("r", companyId, args));
        addEquals(where, args, "d.damage_status", status);
        addEquals(where, args, "x.return_id", returnId);
        addEquals(where, args, "r.rental_id", rentalId);
        addEquals(where, args, "d.damage_id", damageId);

        return jdbc.query("""
                SELECT d.*, x.return_id, r.rental_id, r.rental_status, c.customer_name,
                       e.item_name, u.full_name AS assessed_by_name
                FROM damage_record d
                JOIN return_item x ON x.return_item_id = d.return_item_id
                JOIN rental_item ri ON ri.rental_item_id = x.rental_item_id
                JOIN equipment e ON e.equipment_id = ri.equipment_id
                JOIN rental r ON r.rental_id = ri.rental_id
                JOIN customer c ON c.customer_id = r.customer_id
                LEFT JOIN sys_user u ON u.user_id = d.assessed_by
                """ + where + """
                 ORDER BY FIELD(d.damage_status, 'ASSESSED', 'CHARGED', 'WAIVED', 'REPAIRED'),
                          d.assessment_date DESC
                """, this::damageView, args.toArray());
    }

    public List<LostView> findLostItems(Integer companyId, String status, Integer rentalId, Integer lostItemId) {
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1 = 1").append(companyFilter("r", companyId, args));
        addEquals(where, args, "l.lost_status", status);
        addEquals(where, args, "r.rental_id", rentalId);
        addEquals(where, args, "l.lost_item_id", lostItemId);

        return jdbc.query("""
                SELECT l.*, r.rental_id, r.rental_status, c.customer_name, e.item_name,
                       u.full_name AS reported_by_name
                FROM lost_item l
                JOIN rental_item ri ON ri.rental_item_id = l.rental_item_id
                JOIN equipment e ON e.equipment_id = ri.equipment_id
                JOIN rental r ON r.rental_id = ri.rental_id
                JOIN customer c ON c.customer_id = r.customer_id
                LEFT JOIN sys_user u ON u.user_id = l.reported_by
                """ + where + """
                 ORDER BY FIELD(l.lost_status, 'PENDING', 'CHARGED', 'RECOVERED', 'SETTLED'),
                          l.reported_date DESC
                """, (rs, i) -> new LostView(
                rs.getInt("lost_item_id"),
                rs.getInt("rental_item_id"),
                rs.getInt("rental_id"),
                rs.getString("customer_name"),
                rs.getString("item_name"),
                rs.getInt("quantity_lost"),
                rs.getString("loss_type"),
                rs.getString("reported_date"),
                rs.getString("reason"),
                rs.getBigDecimal("replacement_cost_per_unit"),
                rs.getBigDecimal("charge_amount"),
                rs.getString("reported_by_name"),
                rs.getString("lost_status"),
                rs.getString("notes"),
                rs.getString("rental_status")), args.toArray());
    }

    private DamageView damageView(ResultSet rs, int i) throws SQLException {
        return new DamageView(
                rs.getInt("damage_id"),
                rs.getInt("return_item_id"),
                rs.getInt("return_id"),
                rs.getInt("rental_id"),
                rs.getString("customer_name"),
                rs.getString("item_name"),
                rs.getInt("damaged_quantity"),
                rs.getString("damage_level"),
                rs.getString("damage_description"),
                rs.getBigDecimal("estimated_cost"),
                rs.getBigDecimal("final_charge"),
                rs.getString("assessed_by_name"),
                rs.getString("assessment_date"),
                rs.getString("damage_status"),
                rs.getString("rental_status"));
    }

    // =========================================================
    // Billing (Module 3 tables, read only)
    // =========================================================

    public List<ChargeLine> findCharges(Integer rentalId) {
        return jdbc.query("""
                SELECT charge_id, charge_type, charge_description, amount, charge_date, invoice_id
                FROM charge WHERE rental_id = ?
                ORDER BY charge_date, charge_id
                """, (rs, i) -> new ChargeLine(
                rs.getInt("charge_id"),
                rs.getString("charge_type"),
                rs.getString("charge_description"),
                rs.getBigDecimal("amount"),
                rs.getString("charge_date"),
                (Integer) rs.getObject("invoice_id", Integer.class)), rentalId);
    }

    /** Sum of the refundable deposits on the rental lines (what should have been collected). */
    public BigDecimal calculatedDeposit(Integer rentalId) {
        BigDecimal value = jdbc.queryForObject(
                "SELECT COALESCE(SUM(line_deposit), 0) FROM rental_item WHERE rental_id = ? AND item_status <> 'SELECTED'",
                BigDecimal.class, rentalId);
        return value == null ? BigDecimal.ZERO : value;
    }

    // =========================================================
    // Settlements list and overview numbers
    // =========================================================

    /** Rentals that have equipment back (or partly back) and their settlement state. */
    public List<SettlementRow> findSettlementRows(Integer companyId, String search) {
        List<Object> args = new ArrayList<>();
        String where = companyFilter("r", companyId, args) + searchFilter(search, args);

        return jdbc.query("""
                SELECT r.rental_id, c.customer_name, r.due_date, r.rental_status,
                       """ + UNITS_OUT + """
                 AS units_out,
                       (SELECT COALESCE(SUM(ch.amount), 0) FROM charge ch WHERE ch.rental_id = r.rental_id) AS total_charges,
                       (SELECT COALESCE(SUM(ch.amount), 0) FROM charge ch WHERE ch.rental_id = r.rental_id)
                         - (SELECT COALESCE(SUM(i.amount_paid), 0) FROM invoice i
                             WHERE i.rental_id = r.rental_id AND i.invoice_status <> 'CANCELLED') AS balance_due,
                       s.settlement_status, s.settled_at
                FROM rental r
                JOIN customer c ON c.customer_id = r.customer_id
                LEFT JOIN settlement s ON s.rental_id = r.rental_id
                WHERE (r.rental_status IN ('RETURNED', 'CLOSED', 'PARTIALLY_RETURNED')
                       OR s.settlement_id IS NOT NULL
                       OR EXISTS (SELECT 1 FROM rental_return rr WHERE rr.rental_id = r.rental_id)
                       OR EXISTS (SELECT 1 FROM lost_item l JOIN rental_item ri ON ri.rental_item_id = l.rental_item_id
                                   WHERE ri.rental_id = r.rental_id))
                """ + where + """
                 ORDER BY FIELD(COALESCE(s.settlement_status, 'NONE'), 'PENDING', 'NONE', 'SETTLED'),
                          r.rental_id DESC
                """, (rs, i) -> new SettlementRow(
                rs.getInt("rental_id"),
                rs.getString("customer_name"),
                rs.getString("due_date"),
                rs.getString("rental_status"),
                rs.getInt("units_out"),
                rs.getBigDecimal("total_charges"),
                rs.getBigDecimal("balance_due"),
                rs.getString("settlement_status"),
                rs.getString("settled_at")), args.toArray());
    }

    public Module4Counts counts(Integer companyId) {
        List<Object> args = new ArrayList<>();
        String company = companyFilter("r", companyId, args);
        int n = args.size();
        // the same company filter is used by each sub-query
        List<Object> all = new ArrayList<>();
        for (int k = 0; k < 6; k++) {
            all.addAll(args.subList(0, n));
        }

        return jdbc.queryForObject("""
                SELECT
                  (SELECT COUNT(*) FROM rental r WHERE r.rental_status IN ('ACTIVE', 'OVERDUE', 'PARTIALLY_RETURNED')
                      AND """ + UNITS_OUT + " > 0" + company + """
                  ) AS rentals_out,
                  (SELECT COUNT(*) FROM rental r WHERE r.rental_status IN ('ACTIVE', 'OVERDUE', 'PARTIALLY_RETURNED')
                      AND r.due_date < CURDATE() AND """ + UNITS_OUT + " > 0" + company + """
                  ) AS overdue,
                  (SELECT COUNT(*) FROM damage_record d
                      JOIN return_item x ON x.return_item_id = d.return_item_id
                      JOIN rental_item ri ON ri.rental_item_id = x.rental_item_id
                      JOIN rental r ON r.rental_id = ri.rental_id
                    WHERE d.damage_status IN ('ASSESSED', 'CHARGED')""" + company + """
                  ) AS damages_open,
                  (SELECT COUNT(*) FROM lost_item l
                      JOIN rental_item ri ON ri.rental_item_id = l.rental_item_id
                      JOIN rental r ON r.rental_id = ri.rental_id
                    WHERE l.lost_status = 'PENDING'""" + company + """
                  ) AS lost_pending,
                  (SELECT COUNT(*) FROM rental r
                      LEFT JOIN settlement s ON s.rental_id = r.rental_id
                    WHERE r.rental_status NOT IN ('DRAFT', 'CLOSED', 'CANCELLED')
                      AND EXISTS (SELECT 1 FROM rental_item ri WHERE ri.rental_id = r.rental_id
                                    AND ri.item_status <> 'SELECTED')
                      AND """ + UNITS_OUT + " = 0 " + """
                      AND COALESCE(s.settlement_status, 'PENDING') <> 'SETTLED'""" + company + """
                  ) AS ready_to_settle,
                  (SELECT COUNT(*) FROM rental_return rr JOIN rental r ON r.rental_id = rr.rental_id
                    WHERE DATE(rr.return_date) = CURDATE()""" + company + """
                  ) AS returns_today
                """, (rs, i) -> new Module4Counts(
                rs.getInt("rentals_out"),
                rs.getInt("overdue"),
                rs.getInt("damages_open"),
                rs.getInt("lost_pending"),
                rs.getInt("ready_to_settle"),
                rs.getInt("returns_today")), all.toArray());
    }

    // =========================================================
    // helpers
    // =========================================================

    private static String companyFilter(String alias, Integer companyId, List<Object> args) {
        if (companyId == null) {
            return "";
        }
        args.add(companyId);
        return " AND " + alias + ".company_id = ?";
    }

    /** Matches rental number, renter name or phone. */
    private static String searchFilter(String search, List<Object> args) {
        if (search == null || search.isBlank()) {
            return "";
        }
        String s = search.trim().replace("#", "");
        String like = "%" + s.toLowerCase() + "%";
        args.add(s);
        args.add(like);
        args.add(like);
        return " AND (CAST(r.rental_id AS CHAR) = ? OR LOWER(c.customer_name) LIKE ? OR c.phone LIKE ?)";
    }

    private static void addEquals(StringBuilder where, List<Object> args, String column, Object value) {
        if (value == null || (value instanceof String s && s.isBlank())) {
            return;
        }
        where.append(" AND ").append(column).append(" = ?");
        args.add(value);
    }
}
