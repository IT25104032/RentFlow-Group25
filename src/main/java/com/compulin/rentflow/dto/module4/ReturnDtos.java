package com.compulin.rentflow.dto.module4;

import java.math.BigDecimal;
import java.util.List;

/*
 * MODULE 4 (IT25104066) - request and response DTOs for returns,
 * damage records and lost items. Records keep each shape on one line.
 */
public final class ReturnDtos {

    private ReturnDtos() {
    }

    // Requests
    /** Damage details for units returned DAMAGED / MISSING PARTS / NEEDS MAINTENANCE. */
    public record DamageInput(
            Integer damagedQuantity,
            String damageLevel,
            String damageDescription,
            BigDecimal estimatedCost,
            BigDecimal finalCharge) {
    }

    /** One returned line. A rental item can be split into several lines based on individual item condition */
    public record ReturnLine(
            Integer rentalItemId,
            Integer quantityReturned,
            String conditionStatus,
            String inspectionNotes,
            BigDecimal lateCharge,
            DamageInput damage) {
    }

    /** Units that did not come back. */
    public record LostLine(
            Integer rentalItemId,
            Integer quantityLost,
            String lossType,
            BigDecimal replacementCostPerUnit,
            String reason,
            String notes) {
    }

    public record ProcessReturnRequest(
            Integer rentalId,
            Integer processedBy,
            String returnDate,
            String notes,
            List<ReturnLine> items,
            List<LostLine> lostItems) {
    }

    public record LostItemRequest(
            Integer rentalId,
            Integer reportedBy,
            Integer rentalItemId,
            Integer quantityLost,
            String lossType,
            BigDecimal replacementCostPerUnit,
            String reason,
            String notes) {
    }

    public record ChargeDamageRequest(BigDecimal amount, Integer userId) {
    }

    // Responses
    /** A rental that still has equipment out */
    public record OpenRental(
            Integer rentalId,
            String customerName,
            String customerPhone,
            String startDate,
            String dueDate,
            String rentalStatus,
            int unitsOut,
            long daysOverdue) {
    }

    /** One rental line on the return screen. */
    public record ReturnableItem(
            Integer rentalItemId,
            Integer equipmentId,
            String equipmentName,
            String itemCode,
            BigDecimal ratePerUnit,
            String ratePeriod,
            int issuedQuantity,
            int returnedQuantity,
            int lostQuantity,
            int remainingQuantity,
            String itemStatus,
            BigDecimal lateChargePerUnit) {
    }

    /** What items are still out and the late charge per unit. */
    public record RentalForReturn(
            Integer rentalId,
            Integer companyId,
            String customerName,
            String customerPhone,
            String startDate,
            String dueDate,
            String rentalStatus,
            String returnDate,
            long daysLate,
            List<ReturnableItem> items) {
    }

    public record ProcessReturnResponse(
            Integer returnId,
            Integer rentalId,
            String returnType,
            String rentalStatus,
            int unitsReturned,
            int unitsLost,
            BigDecimal lateCharges,
            BigDecimal damageCharges,
            BigDecimal lostItemCharges,
            boolean readyForSettlement) {
    }

    /** Return History list row. */
    public record ReturnSummary(
            Integer returnId,
            Integer rentalId,
            String customerName,
            String returnDate,
            String returnType,
            String processedByName,
            int totalUnits,
            int damagedUnits,
            String notes) {
    }

    public record ReturnedItemView(
            Integer returnItemId,
            Integer rentalItemId,
            String equipmentName,
            String itemCode,
            int quantityReturned,
            String conditionStatus,
            String inspectionNotes,
            String returnedAt) {
    }

    public record ReturnDetails(
            ReturnSummary summary,
            String rentalStatus,
            String dueDate,
            List<ReturnedItemView> items,
            List<DamageView> damages) {
    }

    public record DamageView(
            Integer damageId,
            Integer returnItemId,
            Integer returnId,
            Integer rentalId,
            String customerName,
            String equipmentName,
            int damagedQuantity,
            String damageLevel,
            String damageDescription,
            BigDecimal estimatedCost,
            BigDecimal finalCharge,
            String assessedByName,
            String assessmentDate,
            String status,
            String rentalStatus) {
    }

    public record LostView(
            Integer lostItemId,
            Integer rentalItemId,
            Integer rentalId,
            String customerName,
            String equipmentName,
            int quantityLost,
            String lossType,
            String reportedDate,
            String reason,
            BigDecimal replacementCostPerUnit,
            BigDecimal chargeAmount,
            String reportedByName,
            String status,
            String notes,
            String rentalStatus) {
    }

    /** Numbers for the Module 4 overview cards and navigation bars */
    public record Module4Counts(
            int rentalsOut,
            int overdue,
            int damagesToResolve,
            int lostPending,
            int readyToSettle,
            int returnsToday) {
    }
}