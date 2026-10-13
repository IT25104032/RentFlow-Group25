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

@Service
public class SecurityDepositService {

    private final SecurityDepositRepository securityDepositRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SecurityDepositService(SecurityDepositRepository securityDepositRepository, JdbcTemplate jdbcTemplate) {
        this.securityDepositRepository = securityDepositRepository;
        this.jdbcTemplate = jdbcTemplate;
    }


     // Retrieve all security deposit records.

    @Transactional
    public List<SecurityDeposit> getAllDeposits() {
        syncFromRentals();
        return securityDepositRepository.findAll();
    }

    @Transactional
    public void syncFromRentals() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT rental_id, SUM(line_deposit) AS total_deposit " +
                        "FROM rental_item GROUP BY rental_id HAVING SUM(line_deposit) > 0");

        for (Map<String, Object> row : rows) {
            Integer rentalId = ((Number) row.get("rental_id")).intValue();
            BigDecimal total = new BigDecimal(row.get("total_deposit").toString());

            Optional<SecurityDeposit> existing = securityDepositRepository.findByRentalId(rentalId);
            if (existing.isEmpty()) {
                initializeDeposit(rentalId, total);
            } else if (existing.get().getDepositStatus() == SecurityDeposit.DepositStatus.PENDING
                    && existing.get().getCalculatedDeposit().compareTo(total) != 0) {
                existing.get().setCalculatedDeposit(total);
                securityDepositRepository.save(existing.get());
            }
        }
    }


     // Retrieve deposit record by primary key

    public SecurityDeposit getDepositById(Integer depositId) {
        return securityDepositRepository.findById(depositId)
                .orElseThrow(() -> new RuntimeException("Security deposit not found with ID: " + depositId));
    }


     // Retrieve deposit record linked to a specific rental ID

     @Transactional
    public SecurityDeposit getDepositByRentalId(Integer rentalId) {
        syncFromRentals();
        return securityDepositRepository.findByRentalId(rentalId)
                .orElseThrow(() -> new RuntimeException("No security deposit found for Rental ID: " + rentalId));
    }


     // Retrieve deposits filtered by status

    public List<SecurityDeposit> getDepositsByStatus(SecurityDeposit.DepositStatus status) {
        return securityDepositRepository.findByDepositStatus(status);
    }


     // Retrieve deposits processed by a specific staff member

    public List<SecurityDeposit> getDepositsByStaff(Integer staffId) {
        return securityDepositRepository.findByReceivedBy(staffId);
    }


     // Creates the PENDING row for a rental

    @Transactional
    public SecurityDeposit initializeDeposit(Integer rentalId, BigDecimal calculatedDeposit) {
        if (rentalId == null || calculatedDeposit == null
                || calculatedDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valid rental ID and non-negative deposit amount are required.");
        }

        // Do not create a second deposit or overwrite a deposit that has been paid.
        SecurityDeposit existing = securityDepositRepository.findByRentalId(rentalId).orElse(null);
        if (existing != null) {
            if (existing.getCalculatedDeposit().compareTo(calculatedDeposit) != 0) {
                throw new IllegalStateException(
                        "Rental already has a security deposit with a different amount: " + rentalId
                );
            }
            return existing;
        }

        SecurityDeposit deposit = new SecurityDeposit();
        deposit.setRentalId(rentalId);
        deposit.setCalculatedDeposit(calculatedDeposit != null ? calculatedDeposit : BigDecimal.ZERO);
        deposit.setDepositAmountReceived(BigDecimal.ZERO);
        deposit.setAmountDeducted(BigDecimal.ZERO);
        deposit.setAmountRefunded(BigDecimal.ZERO);
        deposit.setDepositStatus(SecurityDeposit.DepositStatus.PENDING);

        return securityDepositRepository.save(deposit);
    }


     // Record deposit collection from customer (transitions status to HELD)

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


     // Process deposit refund upon equipment return and inspection
     // amountDeducted is any extra deduction now; amounts already deducted are kept

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
