package com.compulin.rentflow.dto.module4;

import java.math.BigDecimal;
import java.util.List;

/*
 * MODULE 4 (IT25104066) - final settlement shapes.
 */
public final class SettlementDtos {

    private SettlementDtos() {
    }

    /** Settlements list row. */
    public record SettlementRow(
            Integer rentalId,
            String customerName,
            String dueDate,
            String rentalStatus,
            int unitsOut,
            BigDecimal totalCharges,
            BigDecimal balanceDue,
            String settlementStatus,
            String settledAt) {
    }

    public record ChargeLine(
            Integer chargeId,
            String chargeType,
            String description,
            BigDecimal amount,
            String chargeDate,
            Integer invoiceId) {
    }

    /** Preview of the settlement before saving it. */
    public record SettlementPreview(
            Integer rentalId,
            String customerName,
            String customerPhone,
            String startDate,
            String dueDate,
            String rentalStatus,
            int unitsOut,
            boolean allItemsResolved,
            Integer invoiceId,

            BigDecimal rentalCharges,
            BigDecimal lateCharges,
            BigDecimal damageCharges,
            BigDecimal lostItemCharges,
            BigDecimal totalCharges,
            BigDecimal alreadyPaid,
            BigDecimal outstanding,

            String depositStatus,
            BigDecimal depositCalculated,
            BigDecimal depositHeld,
            BigDecimal depositToUse,
            BigDecimal depositToRefund,
            BigDecimal balanceAfterDeposit,

            String settlementStatus,
            String settledAt,
            BigDecimal settledDepositUsed,
            BigDecimal settledDepositRefunded,
            BigDecimal settledFinalBalance,

            List<ChargeLine> charges,
            List<ReturnDtos.ReturnSummary> returns) {
    }

    public record SettleRequest(Integer userId) {
    }

    /** Collect the remaining balance, then settle again. */
    public record SettlementPaymentRequest(
            BigDecimal amount,
            String paymentMethod,
            String referenceNo,
            Integer userId) {
    }
}