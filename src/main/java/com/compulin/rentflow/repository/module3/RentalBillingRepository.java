package com.compulin.rentflow.repository.module3;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Reads the rental data that Module 3 needs for billing
@Repository
public class RentalBillingRepository {

    private final JdbcTemplate jdbcTemplate;

    public RentalBillingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record RentalPeriod(Integer rentalId, LocalDate startDate, LocalDate dueDate, Integer createdBy) {}

    public record RentalItemLine(Integer rentalItemId, String itemName, Integer quantity,
                                 BigDecimal ratePerUnit, String ratePeriod) {}

    public Optional<RentalPeriod> findRentalPeriod(Integer rentalId) {
        List<RentalPeriod> rows = jdbcTemplate.query(
                "SELECT rental_id, start_date, due_date, created_by FROM rental WHERE rental_id = ?",
                (rs, i) -> new RentalPeriod(
                        rs.getInt("rental_id"),
                        rs.getObject("start_date", LocalDate.class),
                        rs.getObject("due_date", LocalDate.class),
                        rs.getInt("created_by")),
                rentalId);
        return rows.stream().findFirst();
    }

    public List<RentalItemLine> findRentalItems(Integer rentalId) {
        return jdbcTemplate.query(
                "SELECT ri.rental_item_id, e.item_name, ri.quantity, ri.rate_per_unit, ri.rate_period " +
                        "FROM rental_item ri JOIN equipment e ON e.equipment_id = ri.equipment_id " +
                        "WHERE ri.rental_id = ? ORDER BY ri.rental_item_id",
                (rs, i) -> new RentalItemLine(
                        rs.getInt("rental_item_id"),
                        rs.getString("item_name"),
                        rs.getInt("quantity"),
                        rs.getBigDecimal("rate_per_unit"),
                        rs.getString("rate_period")),
                rentalId);
    }
}
