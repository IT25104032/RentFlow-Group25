package com.compulin.rentflow.service.module3;

import com.compulin.rentflow.entity.module3.SecurityDeposit;
import com.compulin.rentflow.repository.module3.SecurityDepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityDepositService {

    private final SecurityDepositRepository securityDepositRepository;

    @Autowired
    public SecurityDepositService(SecurityDepositRepository securityDepositRepository) {
        this.securityDepositRepository = securityDepositRepository;
    }

    /**
     * Retrieve all security deposit records
     */
    public List<SecurityDeposit> getAllDeposits() {
        return securityDepositRepository.findAll();
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
    public SecurityDeposit getDepositByRentalId(Integer rentalId) {
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
     * Initial record creation when a rental booking is placed
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
     * Process deposit return/refund upon equipment return and inspection
     */
    @Transactional
    public SecurityDeposit processDepositReturn(Integer depositId, BigDecimal amountDeducted) {
        SecurityDeposit deposit = getDepositById(depositId);

        BigDecimal received = deposit.getDepositAmountReceived() != null
                ? deposit.getDepositAmountReceived()
                : BigDecimal.ZERO;

        BigDecimal deduction = amountDeducted != null ? amountDeducted : BigDecimal.ZERO;

        if (deduction.compareTo(received) > 0) {
            throw new RuntimeException("Deduction amount cannot exceed the received deposit (" + received + ")");
        }

        BigDecimal refundAmount = received.subtract(deduction);

        deposit.setAmountDeducted(deduction);
        deposit.setAmountRefunded(refundAmount);
        deposit.setRefundDate(LocalDateTime.now());

        // Update status based on deduction
        if (refundAmount.compareTo(BigDecimal.ZERO) == 0 && received.compareTo(BigDecimal.ZERO) > 0) {
            deposit.setDepositStatus(SecurityDeposit.DepositStatus.FORFEITED);
        } else if (deduction.compareTo(BigDecimal.ZERO) > 0) {
            deposit.setDepositStatus(SecurityDeposit.DepositStatus.PARTIALLY_REFUNDED);
        } else {
            deposit.setDepositStatus(SecurityDeposit.DepositStatus.REFUNDED);
        }

        return securityDepositRepository.save(deposit);
    }
}