package com.compulin.rentflow.service.module4;

import com.compulin.rentflow.dto.module4.ReturnDtos.LostLine;
import com.compulin.rentflow.dto.module4.ReturnDtos.LostView;
import com.compulin.rentflow.entity.module4.LostItem;
import com.compulin.rentflow.repository.module4.LostItemRepository;
import com.compulin.rentflow.repository.module4.RentalLookupRepository;
import com.compulin.rentflow.repository.module4.SettlementRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.compulin.rentflow.service.module4.Module4Support.asInt;
import static com.compulin.rentflow.service.module4.Module4Support.blankToNull;
import static com.compulin.rentflow.service.module4.Module4Support.money;

/*
 * MODULE 4 (IT25104066) - lost notes.
 *
 * Recording a loss:
 *   - can never exceed the units still out on that rental line
 *   - charges quantity x replacement cost (Module 3 LOST_ITEM charge)
 *   - writes the units out of the company's stock (Module 1 total_quantity)
 * Recovering a lost item puts the stock back and removes the charge.
 */
@Service
public class LostItemService {

    private final LostItemRepository lostItemRepository;
    private final SettlementRepository settlementRepository;
    private final RentalLookupRepository lookup;
    private final Module4Support support;
    private final Module4Billing billing;

    // stock (Module 1 table)
    private final Module4Links links;

    public LostItemService(LostItemRepository lostItemRepository,
                           SettlementRepository settlementRepository,
                           RentalLookupRepository lookup,
                           Module4Support support,
                           Module4Billing billing,
                           Module4Links links) {
        this.lostItemRepository = lostItemRepository;
        this.settlementRepository = settlementRepository;
        this.lookup = lookup;
        this.support = support;
        this.billing = billing;
        this.links = links;
    }

    public List<LostView> list(Integer companyId, String status, Integer rentalId) {
        return lookup.findLostItems(companyId, status, rentalId, null);
    }

    /** Lost note on its own (from the Lost Items page). */
    @Transactional
    public LostView recordLost(Integer companyId, Integer rentalId, LostLine line, Integer userId) {
        Map<String, Object> rental = support.rental(rentalId, companyId);
        if (!Module4Support.OPEN_STATUSES.contains((String) rental.get("rental_status"))) {
            throw new IllegalArgumentException(
                    "Lost items can only be recorded while equipment is out (rental is "
                            + rental.get("rental_status") + ").");
        }
        Map<String, Object> item = findLine(rentalId, line.rentalItemId());
        int out = asInt(item.get("quantity")) - asInt(item.get("returned_qty")) - asInt(item.get("lost_qty"));
        validate(line, out, (String) item.get("item_name"));

        LostItem saved = save(rentalId, item, line, userId);
        support.refreshStatuses(rentalId);
        return lookup.findLostItems(null, null, null, saved.getLostItemId()).get(0);
    }

    /**
     * Saves a lost note whose quantity was already validated by the caller
     * (ProcessReturn validates returned + lost together). Returns the charge.
     */
    LostItem save(Integer rentalId, Map<String, Object> item, LostLine line, Integer userId) {
        BigDecimal unitCost = money(line.replacementCostPerUnit());
        String name = (String) item.get("item_name");

        LostItem lost = new LostItem();
        lost.setRentalItemId(line.rentalItemId());
        lost.setQuantityLost(line.quantityLost());
        lost.setLossType(line.lossType().trim().toUpperCase());
        lost.setReportedDate(LocalDateTime.now());
        lost.setReason(blankToNull(line.reason()));
        lost.setReplacementCostPerUnit(unitCost);
        lost.setChargeAmount(money(unitCost.multiply(BigDecimal.valueOf(line.quantityLost()))));
        lost.setReportedBy(userId);
        lost.setNotes(blankToNull(line.notes()));
        lost.setLostStatus("PENDING");
        lost = lostItemRepository.save(lost);

        if (Module4Support.isPositive(lost.getChargeAmount())) {
            billing.addCharge(rentalId, line.rentalItemId(), Module4Billing.LOST_ITEM,
                    Module4Billing.lostLabel(lost.getLostItemId()) + ": " + name + " x" + lost.getQuantityLost()
                            + " " + lost.getLossType().toLowerCase().replace('_', '-'),
                    lost.getChargeAmount(), userId);
            lost.setLostStatus("CHARGED");
        }

        // INTEGRATION (Module 4 -> Module 1): lost units leave the company's stock.
        links.writeOffStock(asInt(item.get("equipment_id")), lost.getQuantityLost());

        return lostItemRepository.saveAndFlush(lost);
    }

    /** The lost units were found and handed back. */
    @Transactional
    public LostView recover(Integer companyId, Integer lostItemId) {
        LostItem lost = lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new IllegalArgumentException("Lost item not found."));
        LostView view = lookup.findLostItems(null, null, null, lostItemId).get(0);
        support.rental(view.rentalId(), companyId);

        if (!List.of("PENDING", "CHARGED").contains(lost.getLostStatus())) {
            throw new IllegalArgumentException("This lost note is already " + lost.getLostStatus().toLowerCase() + ".");
        }
        requireNotSettled(view.rentalId());

        billing.removeCharges(view.rentalId(), Module4Billing.LOST_ITEM, Module4Billing.lostLabel(lostItemId));

        Map<String, Object> item = findLine(view.rentalId(), lost.getRentalItemId());
        links.restoreStock(asInt(item.get("equipment_id")), lost.getQuantityLost());

        lost.setLostStatus("RECOVERED");
        lost.setChargeAmount(BigDecimal.ZERO);
        lostItemRepository.saveAndFlush(lost);
        return lookup.findLostItems(null, null, null, lostItemId).get(0);
    }

    /** Puts a charge on a lost note that was recorded without one. */
    @Transactional
    public LostView charge(Integer companyId, Integer lostItemId, BigDecimal costPerUnit, Integer userId) {
        LostItem lost = lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new IllegalArgumentException("Lost item not found."));
        LostView view = lookup.findLostItems(null, null, null, lostItemId).get(0);
        support.rental(view.rentalId(), companyId);
        if (!"PENDING".equals(lost.getLostStatus())) {
            throw new IllegalArgumentException("Only a PENDING lost note can be charged.");
        }
        if (!Module4Support.isPositive(costPerUnit)) {
            throw new IllegalArgumentException("Replacement cost must be greater than zero.");
        }
        requireNotSettled(view.rentalId());

        lost.setReplacementCostPerUnit(money(costPerUnit));
        lost.setChargeAmount(money(costPerUnit.multiply(BigDecimal.valueOf(lost.getQuantityLost()))));
        billing.addCharge(view.rentalId(), lost.getRentalItemId(), Module4Billing.LOST_ITEM,
                Module4Billing.lostLabel(lostItemId) + ": " + view.equipmentName() + " x" + lost.getQuantityLost()
                        + " " + lost.getLossType().toLowerCase().replace('_', '-'),
                lost.getChargeAmount(), userId);
        lost.setLostStatus("CHARGED");
        lostItemRepository.saveAndFlush(lost);
        return lookup.findLostItems(null, null, null, lostItemId).get(0);
    }

    static void validate(LostLine line, int out, String name) {
        if (line.quantityLost() == null || line.quantityLost() <= 0) {
            throw new IllegalArgumentException("Lost quantity for " + name + " must be greater than zero.");
        }
        if (line.lossType() == null || !Module4Support.LOSS_TYPES.contains(line.lossType().trim().toUpperCase())) {
            throw new IllegalArgumentException("Loss type must be LOST, STOLEN or NON_RETURNED.");
        }
        if (line.replacementCostPerUnit() != null && line.replacementCostPerUnit().signum() < 0) {
            throw new IllegalArgumentException("Replacement cost cannot be negative.");
        }
        if (line.quantityLost() > out) {
            throw new IllegalArgumentException("Only " + out + " unit(s) of " + name
                    + " are still out, so at most " + out + " can be recorded as lost.");
        }
    }

    Map<String, Object> findLine(Integer rentalId, Integer rentalItemId) {
        if (rentalItemId == null) {
            throw new IllegalArgumentException("Choose the rental item.");
        }
        return lookup.findIssuedItems(rentalId).stream()
                .filter(i -> rentalItemId.equals(asInt(i.get("rental_item_id"))))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Rental item #" + rentalItemId + " is not an issued item of rental #" + rentalId + "."));
    }

    private void requireNotSettled(Integer rentalId) {
        settlementRepository.findByRentalId(rentalId)
                .filter(s -> "SETTLED".equals(s.getSettlementStatus()))
                .ifPresent(s -> {
                    throw new IllegalArgumentException("Rental #" + rentalId + " is already settled.");
                });
    }
}
