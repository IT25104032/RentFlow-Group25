package com.compulin.rentflow.service.module3;

import com.compulin.rentflow.entity.module3.SecurityDeposit;
import com.compulin.rentflow.repository.module3.SecurityDepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SecurityDepositService {

    private final SecurityDepositRepository securityDepositRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SecurityDepositService(SecurityDepositRepository securityDepositRepository, JdbcTemplate jdbcTemplate) {
        this.securityDepositRepository = securityDepositRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Retrieve all security deposit records.
     * Runs syncFromRentals() first so new rentals from Module 2 show up automatically.
     */
    @Transactional
    public List<SecurityDeposit> getAllDeposits() {
        syncFromRentals();
        return securityDepositRepository.findAll();
    }

    /**
     * Restore the officer's agreed deposit for PENDING rentals affected by a
     * previous sync. Existing collected/refunded records are never modified.
     * Old rentals without an agreed-deposit marker retain their existing amount.
     */
    private static final Pattern FINAL_DEPOSIT_PATTERN =
            Pattern.compile("\\[FINAL_DEPOSIT=([0-9]+(?:\\.[0-9]+)?)\\]");

    @Transactional
    public void syncFromRentals() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT r.rental_id, r.notes, COALESCE(SUM(ri.line_deposit), 0) AS total_deposit " +
                        "FROM rental r LEFT JOIN rental_item ri ON ri.rental_id = r.rental_id " +
                        "GROUP BY r.rental_id, r.notes");

        for (Map<String, Object> row : rows) {
            Integer rentalId = ((Number) row.get("rental_id")).intValue();
            BigDecimal lineTotal = new BigDecimal(row.get("total_deposit").toString());
            String notes = row.get("notes") == null ? "" : row.get("notes").toString();
            Matcher matcher = FINAL_DEPOSIT_PATTERN.matcher(notes);
            BigDecimal agreedDeposit = null;
            while (matcher.find()) {
                agreedDeposit = new BigDecimal(matcher.group(1));
            }

            Optional<SecurityDeposit> existing = securityDepositRepository.findByRentalId(rentalId);
            if (existing.isEmpty()) {
                BigDecimal amount = agreedDeposit != null ? agreedDeposit : lineTotal;
                if (agreedDeposit != null || amount.compareTo(BigDecimal.ZERO) > 0) {
                    initializeDeposit(rentalId, amount);
                }
            } else if (agreedDeposit != null
                    && existing.get().getDepositStatus() == SecurityDeposit.DepositStatus.PENDING
                    && existing.get().getCalculatedDeposit().compareTo(agreedDeposit) != 0) {
                SecurityDeposit deposit = existing.get();
                deposit.setCalculatedDeposit(agreedDeposit);
                securityDepositRepository.save(deposit);
            }
        }
    }

    /**
     * Retrieve deposit record by primary key
     */
    public SecurityDeposit getDepositById(Integer depositId) {
        return securityDepositRepository.findById(depositId)
                .orElseThrow(() -> new RuntimeException("Security deposit not found with ID: " + depositId));
    }

    /**
     * Retrieve deposit record linked to a specific rental ID
     */
    @Transactional
    public SecurityDeposit getDepositByRentalId(Integer rentalId) {
        syncFromRentals();
        return securityDepositRepository.findByRentalId(rentalId)
                .orElseThrow(() -> new RuntimeException("No security deposit found for Rental ID: " + rentalId));
    }

    /**
     * Retrieve deposits filtered by status (PENDING, HELD, REFUNDED, etc.)
     */
    public List<SecurityDeposit> getDepositsByStatus(SecurityDeposit.DepositStatus status) {
        return securityDepositRepository.findByDepositStatus(status);
    }

    /**
     * Retrieve deposits processed by a specific staff member
     */
    public List<SecurityDeposit> getDepositsByStaff(Integer staffId) {
        return securityDepositRepository.findByReceivedBy(staffId);
    }

    /**
     * Creates the PENDING row for a rental (called by syncFromRentals, not by the user)
     */
    @Transactional
    public SecurityDeposit initializeDeposit(Integer rentalId, BigDecimal calculatedDeposit) {
        SecurityDeposit deposit = new SecurityDeposit();
        deposit.setRentalId(rentalId);
        deposit.setCalculatedDeposit(calculatedDeposit != null ? calculatedDeposit : BigDecimal.ZERO);
        deposit.setDepositAmountReceived(BigDecimal.ZERO);
        deposit.setAmountDeducted(BigDecimal.ZERO);
        deposit.setAmountRefunded(BigDecimal.ZERO);
        deposit.setDepositStatus(SecurityDeposit.DepositStatus.PENDING);

        return securityDepositRepository.save(deposit);
    }

    /**
     * Record deposit collection from customer (transitions status to HELD)
     */
    @Transactional
    public SecurityDeposit receiveDeposit(Integer depositId, BigDecimal amountReceived, Integer receivedByStaffId) {
        SecurityDeposit deposit = getDepositById(depositId);

        if (amountReceived == null || amountReceived.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Received deposit amount must be greater than zero.");
        }

        deposit.setDepositAmountReceived(amountReceived);
        deposit.setReceivedDate(LocalDateTime.now());
        deposit.setReceivedBy(receivedByStaffId);
        deposit.setDepositStatus(SecurityDeposit.DepositStatus.HELD);

        return securityDepositRepository.save(deposit);
    }

    /**
     * Process deposit return/refund upon equipment return and inspection.
     * amountDeducted is any extra deduction now; amounts already deducted (e.g. used on an invoice) are kept.
     */
    @Transactional
    public SecurityDeposit processDepositReturn(Integer depositId, BigDecimal amountDeducted) {
        SecurityDeposit deposit = getDepositById(depositId);

        if (deposit.getDepositStatus() != SecurityDeposit.DepositStatus.HELD) {
            throw new RuntimeException("Only a HELD deposit can be refunded.");
        }

        BigDecimal received = orZero(deposit.getDepositAmountReceived());
        BigDecimal alreadyDeducted = orZero(deposit.getAmountDeducted());
        BigDecimal available = received.subtract(alreadyDeducted);

        BigDecimal deduction = orZero(amountDeducted);
        if (deduction.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Deduction cannot be negative.");
        }
        if (deduction.compareTo(available) > 0) {
            throw new RuntimeException("Deduction cannot exceed the remaining deposit (" + available + ")");
        }

        BigDecimal totalDeducted = alreadyDeducted.add(deduction);
        BigDecimal refundAmount = received.subtract(totalDeducted);

        deposit.setAmountDeducted(totalDeducted);
        deposit.setAmountRefunded(refundAmount);
        deposit.setRefundDate(LocalDateTime.now());

        // Update status based on what was kept
        if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {
            deposit.setDepositStatus(SecurityDeposit.DepositStatus.FORFEITED);
        } else if (totalDeducted.compareTo(BigDecimal.ZERO) > 0) {
            deposit.setDepositStatus(SecurityDeposit.DepositStatus.PARTIALLY_REFUNDED);
        } else {
            deposit.setDepositStatus(SecurityDeposit.DepositStatus.REFUNDED);
        }

        return securityDepositRepository.save(deposit);
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
