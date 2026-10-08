package com.compulin.rentflow.service.module4;

import com.compulin.rentflow.dto.module4.ReturnDtos.DamageInput;
import com.compulin.rentflow.dto.module4.ReturnDtos.DamageView;
import com.compulin.rentflow.entity.module4.DamageRecord;
import com.compulin.rentflow.entity.module4.ReturnItem;
import com.compulin.rentflow.repository.module4.DamageRecordRepository;
import com.compulin.rentflow.repository.module4.RentalLookupRepository;
import com.compulin.rentflow.repository.module4.SettlementRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.compulin.rentflow.service.module4.Module4Support.money;

/*
 * MODULE 4 (IT25104066) - damage assessment.
 *
 * Damage is recorded while processing a return (units returned DAMAGED,
 * MISSING PARTS or NEEDS MAINTENANCE). Those units stay out of stock.
 *
 *   ASSESSED  -> recorded, no charge yet          -> Charge / Waive / Repaired
 *   CHARGED   -> charge added to the bill (Mod 3) -> Waive / Repaired
 *   WAIVED    -> charge removed                   -> Repaired
 *   REPAIRED  -> units back in available stock (Module 1)
 */
@Service
public class DamageService {

    private final DamageRecordRepository damageRecordRepository;
    private final SettlementRepository settlementRepository;
    private final RentalLookupRepository lookup;
    private final Module4Support support;
    private final Module4Billing billing;

    // stock (Module 1 table)
    private final Module4Links links;

    public DamageService(DamageRecordRepository damageRecordRepository,
                         SettlementRepository settlementRepository,
                         RentalLookupRepository lookup,
                         Module4Support support,
                         Module4Billing billing,
                         Module4Links links) {
        this.damageRecordRepository = damageRecordRepository;
        this.settlementRepository = settlementRepository;
        this.lookup = lookup;
        this.support = support;
        this.billing = billing;
        this.links = links;
    }

    public List<DamageView> list(Integer companyId, String status, Integer returnId, Integer rentalId) {
        return lookup.findDamages(companyId, status, returnId, rentalId, null);
    }

    public DamageView get(Integer companyId, Integer damageId) {
        List<DamageView> found = lookup.findDamages(companyId, null, null, null, damageId);
        if (found.isEmpty()) {
            throw new IllegalArgumentException("Damage record not found.");
        }
        return found.get(0);
    }

    /** Validates the damage part of a return line before anything is saved. */
    static void validate(DamageInput damage, int quantityReturned, String name, String condition) {
        if (damage == null) {
            throw new IllegalArgumentException("Add damage details for " + name + " returned as "
                    + condition.toLowerCase() + ".");
        }
        if (damage.damageDescription() == null || damage.damageDescription().isBlank()) {
            throw new IllegalArgumentException("Describe the damage on " + name + ".");
        }
        if (damage.damageLevel() == null
                || !Module4Support.DAMAGE_LEVELS.contains(damage.damageLevel().trim().toUpperCase())) {
            throw new IllegalArgumentException("Damage level must be MINOR, MODERATE or SEVERE.");
        }
        int qty = damage.damagedQuantity() == null ? quantityReturned : damage.damagedQuantity();
        if (qty <= 0 || qty > quantityReturned) {
            throw new IllegalArgumentException("Damaged quantity for " + name
                    + " must be between 1 and the " + quantityReturned + " returned.");
        }
        if (isNegative(damage.estimatedCost()) || isNegative(damage.finalCharge())) {
            throw new IllegalArgumentException("Damage amounts cannot be negative.");
        }
    }

    /** Saves the damage record for a return line; a charge above zero is billed at once. */
    DamageRecord record(Integer rentalId, ReturnItem returnItem, DamageInput damage,
                        String equipmentName, Integer userId) {
        DamageRecord d = new DamageRecord();
        d.setReturnItem(returnItem);
        d.setDamagedQuantity(damage.damagedQuantity() == null
                ? returnItem.getQuantityReturned()
                : damage.damagedQuantity());
        d.setDamageDescription(damage.damageDescription().trim());
        d.setDamageLevel(damage.damageLevel().trim().toUpperCase());
        d.setEstimatedCost(money(damage.estimatedCost()));
        d.setFinalCharge(money(damage.finalCharge()));
        d.setAssessedBy(userId);
        d.setAssessmentDate(LocalDateTime.now());
        d.setStatus("ASSESSED");
        d = damageRecordRepository.save(d);

        if (Module4Support.isPositive(d.getFinalCharge())) {
            billDamage(rentalId, returnItem.getRentalItemId(), d, equipmentName, userId);
        }
        return damageRecordRepository.saveAndFlush(d);
    }

    /** Charges an ASSESSED record (e.g. after the repair quote came in). */
    @Transactional
    public DamageView charge(Integer companyId, Integer damageId, BigDecimal amount, Integer userId) {
        DamageView view = get(companyId, damageId);
        DamageRecord d = find(damageId);
        if (!"ASSESSED".equals(d.getStatus())) {
            throw new IllegalArgumentException("Only an ASSESSED damage record can be charged.");
        }
        if (!Module4Support.isPositive(amount)) {
            throw new IllegalArgumentException("Charge must be greater than zero.");
        }
        requireNotSettled(view.rentalId());

        d.setFinalCharge(money(amount));
        billDamage(view.rentalId(), d.getReturnItem().getRentalItemId(), d, view.equipmentName(), userId);
        damageRecordRepository.saveAndFlush(d);
        return get(companyId, damageId);
    }

    /** Removes the damage charge from the bill. */
    @Transactional
    public DamageView waive(Integer companyId, Integer damageId) {
        DamageView view = get(companyId, damageId);
        DamageRecord d = find(damageId);
        if (!List.of("ASSESSED", "CHARGED").contains(d.getStatus())) {
            throw new IllegalArgumentException("This damage record is already " + d.getStatus().toLowerCase() + ".");
        }
        requireNotSettled(view.rentalId());

        billing.removeCharges(view.rentalId(), Module4Billing.DAMAGE, Module4Billing.damageLabel(damageId));
        d.setFinalCharge(BigDecimal.ZERO);
        d.setStatus("WAIVED");
        damageRecordRepository.saveAndFlush(d);
        return get(companyId, damageId);
    }

    /** Repaired units go back into available stock (Module 1). The charge stays on the bill. */
    @Transactional
    public DamageView markRepaired(Integer companyId, Integer damageId) {
        get(companyId, damageId);
        DamageRecord d = find(damageId);
        if ("REPAIRED".equals(d.getStatus())) {
            throw new IllegalArgumentException("Already marked as repaired.");
        }
        links.addToAvailableStock(links.equipmentOfRentalItem(d.getReturnItem().getRentalItemId()),
                d.getDamagedQuantity());
        d.setStatus("REPAIRED");
        damageRecordRepository.saveAndFlush(d);
        return get(companyId, damageId);
    }

    private void billDamage(Integer rentalId, Integer rentalItemId, DamageRecord d,
                            String equipmentName, Integer userId) {
        billing.addCharge(rentalId, rentalItemId, Module4Billing.DAMAGE,
                Module4Billing.damageLabel(d.getDamageId()) + ": " + d.getDamageLevel().toLowerCase() + ", "
                        + equipmentName + " x" + d.getDamagedQuantity() + " - " + d.getDamageDescription(),
                d.getFinalCharge(), userId);
        d.setStatus("CHARGED");
    }

    private DamageRecord find(Integer damageId) {
        return damageRecordRepository.findById(damageId)
                .orElseThrow(() -> new IllegalArgumentException("Damage record not found."));
    }

    private void requireNotSettled(Integer rentalId) {
        settlementRepository.findByRentalId(rentalId)
                .filter(s -> "SETTLED".equals(s.getSettlementStatus()))
                .ifPresent(s -> {
                    throw new IllegalArgumentException(
                            "Rental #" + rentalId + " is already settled, so its charges cannot change.");
                });
        support.rental(rentalId, null);
    }

    private static boolean isNegative(BigDecimal value) {
        return value != null && value.signum() < 0;
    }
}
