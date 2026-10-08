package com.compulin.rentflow.service.module4;

import com.compulin.rentflow.dto.module4.ReturnDtos.LostLine;
import com.compulin.rentflow.dto.module4.ReturnDtos.Module4Counts;
import com.compulin.rentflow.dto.module4.ReturnDtos.OpenRental;
import com.compulin.rentflow.dto.module4.ReturnDtos.ProcessReturnRequest;
import com.compulin.rentflow.dto.module4.ReturnDtos.ProcessReturnResponse;
import com.compulin.rentflow.dto.module4.ReturnDtos.RentalForReturn;
import com.compulin.rentflow.dto.module4.ReturnDtos.ReturnDetails;
import com.compulin.rentflow.dto.module4.ReturnDtos.ReturnLine;
import com.compulin.rentflow.dto.module4.ReturnDtos.ReturnSummary;
import com.compulin.rentflow.dto.module4.ReturnDtos.ReturnableItem;
import com.compulin.rentflow.entity.module4.DamageRecord;
import com.compulin.rentflow.entity.module4.LostItem;
import com.compulin.rentflow.entity.module4.RentalReturn;
import com.compulin.rentflow.entity.module4.ReturnItem;
import com.compulin.rentflow.repository.module4.RentalLookupRepository;
import com.compulin.rentflow.repository.module4.RentalReturnRepository;
import com.compulin.rentflow.repository.module4.ReturnItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.compulin.rentflow.service.module4.Module4Support.asInt;
import static com.compulin.rentflow.service.module4.Module4Support.blankToNull;
import static com.compulin.rentflow.service.module4.Module4Support.money;

/*
 * MODULE 4 (IT25104066) - process return and return history.
 *
 * One "Process Return" saves, in a single transaction:
 *   - the rental_return + one return_item per returned line
 *     (a rental line can be split: 2 GOOD + 1 DAMAGED)
 *   - a damage_record for every non-GOOD line (units stay out of stock)
 *   - a lost_item for units that did not come back
 *   - LATE / DAMAGE / LOST_ITEM charges on the bill (Module 3)
 *   - GOOD units back into available stock (Module 1)
 *   - new rental / rental_item statuses (Module 2)
 */
@Service
public class RentalReturnService {

    private final RentalReturnRepository rentalReturnRepository;
    private final ReturnItemRepository returnItemRepository;
    private final RentalLookupRepository lookup;
    private final Module4Support support;
    private final Module4Billing billing;
    private final DamageService damageService;
    private final LostItemService lostItemService;

    // stock (Module 1 table)
    private final Module4Links links;

    public RentalReturnService(RentalReturnRepository rentalReturnRepository,
                               ReturnItemRepository returnItemRepository,
                               RentalLookupRepository lookup,
                               Module4Support support,
                               Module4Billing billing,
                               DamageService damageService,
                               LostItemService lostItemService,
                               Module4Links links) {
        this.rentalReturnRepository = rentalReturnRepository;
        this.returnItemRepository = returnItemRepository;
        this.lookup = lookup;
        this.support = support;
        this.billing = billing;
        this.damageService = damageService;
        this.lostItemService = lostItemService;
        this.links = links;
    }

    // =========================================================
    // Lookups
    // =========================================================

    public List<OpenRental> openRentals(Integer companyId, String search) {
        return lookup.findOpenRentals(companyId, search);
    }

    public Module4Counts counts(Integer companyId) {
        return lookup.counts(companyId);
    }

    /** Process Return step 2: what is still out and the late charge per unit for the return date. */
    public RentalForReturn rentalForReturn(Integer rentalId, Integer companyId, String returnDate) {
        Map<String, Object> rental = support.rental(rentalId, companyId);
        LocalDateTime when = parseDate(returnDate);
        LocalDate due = LocalDate.parse(rental.get("due_date").toString());
        long daysLate = Math.max(0, ChronoUnit.DAYS.between(due, when.toLocalDate()));

        List<ReturnableItem> items = new ArrayList<>();
        for (Map<String, Object> i : lookup.findIssuedItems(rentalId)) {
            int issued = asInt(i.get("quantity"));
            int returned = asInt(i.get("returned_qty"));
            int lost = asInt(i.get("lost_qty"));
            BigDecimal rate = (BigDecimal) i.get("rate_per_unit");
            String period = (String) i.get("rate_period");
            items.add(new ReturnableItem(
                    asInt(i.get("rental_item_id")),
                    asInt(i.get("equipment_id")),
                    (String) i.get("item_name"),
                    (String) i.get("item_code"),
                    rate,
                    period,
                    issued,
                    returned,
                    lost,
                    Math.max(0, issued - returned - lost),
                    (String) i.get("item_status"),
                    Module4Support.lateChargePerUnit(rate, period, daysLate)));
        }

        return new RentalForReturn(
                rentalId,
                asInt(rental.get("company_id")),
                (String) rental.get("customer_name"),
                (String) rental.get("phone"),
                rental.get("start_date").toString(),
                rental.get("due_date").toString(),
                (String) rental.get("rental_status"),
                when.withSecond(0).withNano(0).toString(),
                daysLate,
                items);
    }

    public List<ReturnSummary> history(Integer companyId, String search, Integer rentalId) {
        return lookup.findReturns(companyId, search, rentalId, null);
    }

    public ReturnDetails details(Integer companyId, Integer returnId) {
        List<ReturnSummary> found = lookup.findReturns(companyId, null, null, returnId);
        if (found.isEmpty()) {
            throw new IllegalArgumentException("Return #" + returnId + " was not found.");
        }
        ReturnSummary summary = found.get(0);
        Map<String, Object> rental = support.rental(summary.rentalId(), companyId);
        return new ReturnDetails(
                summary,
                (String) rental.get("rental_status"),
                rental.get("due_date").toString(),
                lookup.findReturnedItems(returnId),
                lookup.findDamages(companyId, null, returnId, null, null));
    }

    // =========================================================
    // Process return
    // =========================================================

    @Transactional
    public ProcessReturnResponse processReturn(ProcessReturnRequest request, Integer userId, Integer companyId) {
        if (request == null) {
            throw new IllegalArgumentException("Return request is required.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("Please log in before processing a return.");
        }
        Map<String, Object> rental = support.rental(request.rentalId(), companyId);
        Integer rentalId = request.rentalId();
        String status = (String) rental.get("rental_status");
        if (!Module4Support.OPEN_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Rental #" + rentalId + " is " + status
                    + "; only rentals with equipment out can be returned.");
        }

        List<ReturnLine> lines = request.items() == null ? List.of() : request.items();
        List<LostLine> lostLines = request.lostItems() == null ? List.of() : request.lostItems();
        if (lines.isEmpty() && lostLines.isEmpty()) {
            throw new IllegalArgumentException("Select at least one item to return or record as lost.");
        }

        LocalDateTime returnedAt = parseDate(request.returnDate());
        LocalDate start = LocalDate.parse(rental.get("start_date").toString());
        if (returnedAt.toLocalDate().isBefore(start)) {
            throw new IllegalArgumentException("Return date cannot be before the rental start date (" + start + ").");
        }
        if (returnedAt.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new IllegalArgumentException("Return date cannot be in the future.");
        }
        LocalDate due = LocalDate.parse(rental.get("due_date").toString());
        long daysLate = Math.max(0, ChronoUnit.DAYS.between(due, returnedAt.toLocalDate()));

        // ---- 1. validate everything before saving anything ----
        Map<Integer, Map<String, Object>> items = new HashMap<>();
        for (Map<String, Object> i : lookup.findIssuedItems(rentalId)) {
            items.put(asInt(i.get("rental_item_id")), i);
        }
        Map<Integer, Integer> requested = new LinkedHashMap<>();

        for (ReturnLine line : lines) {
            Map<String, Object> item = requireItem(items, line.rentalItemId(), rentalId);
            String name = (String) item.get("item_name");
            if (line.quantityReturned() == null || line.quantityReturned() <= 0) {
                throw new IllegalArgumentException("Return quantity for " + name + " must be greater than zero.");
            }
            String condition = normaliseCondition(line.conditionStatus());
            if (!"GOOD".equals(condition)) {
                DamageService.validate(line.damage(), line.quantityReturned(), name, condition);
            }
            if (line.lateCharge() != null && line.lateCharge().signum() < 0) {
                throw new IllegalArgumentException("Late charge for " + name + " cannot be negative.");
            }
            requested.merge(line.rentalItemId(), line.quantityReturned(), Integer::sum);
        }
        for (LostLine line : lostLines) {
            Map<String, Object> item = requireItem(items, line.rentalItemId(), rentalId);
            LostItemService.validate(line, Integer.MAX_VALUE, (String) item.get("item_name"));
            requested.merge(line.rentalItemId(), line.quantityLost(), Integer::sum);
        }
        for (Map.Entry<Integer, Integer> entry : requested.entrySet()) {
            Map<String, Object> item = items.get(entry.getKey());
            int out = asInt(item.get("quantity")) - asInt(item.get("returned_qty")) - asInt(item.get("lost_qty"));
            if (entry.getValue() > out) {
                throw new IllegalArgumentException("Only " + out + " unit(s) of " + item.get("item_name")
                        + " are still out; returned plus lost cannot be more than that.");
            }
        }

        // ---- 2. the return and its lines ----
        RentalReturn rentalReturn = null;
        BigDecimal lateTotal = BigDecimal.ZERO;
        BigDecimal damageTotal = BigDecimal.ZERO;
        BigDecimal lostTotal = BigDecimal.ZERO;
        int unitsReturned = 0;
        int unitsLost = 0;

        if (!lines.isEmpty()) {
            rentalReturn = new RentalReturn();
            rentalReturn.setRentalId(rentalId);
            rentalReturn.setProcessedBy(userId);
            rentalReturn.setReturnDate(returnedAt);
            rentalReturn.setNotes(blankToNull(request.notes()));
            rentalReturn.setReturnType("PARTIAL");
            rentalReturn = rentalReturnRepository.save(rentalReturn);
        }

        for (ReturnLine line : lines) {
            Map<String, Object> item = items.get(line.rentalItemId());
            String name = (String) item.get("item_name");
            String condition = normaliseCondition(line.conditionStatus());

            ReturnItem returnItem = new ReturnItem();
            returnItem.setRentalReturn(rentalReturn);
            returnItem.setRentalItemId(line.rentalItemId());
            returnItem.setQuantityReturned(line.quantityReturned());
            returnItem.setConditionStatus(condition);
            returnItem.setInspectionNotes(blankToNull(line.inspectionNotes()));
            returnItem.setReturnedAt(returnedAt);
            returnItem = returnItemRepository.save(returnItem);
            unitsReturned += line.quantityReturned();

            // damaged units are held out of stock until repaired
            int heldBack = 0;
            if (!"GOOD".equals(condition)) {
                DamageRecord damage = damageService.record(rentalId, returnItem, line.damage(), name, userId);
                heldBack = damage.getDamagedQuantity();
                damageTotal = damageTotal.add(money(damage.getFinalCharge()));
            }

            // INTEGRATION (Module 4 -> Module 1): good units can be rented again
            int backToStock = line.quantityReturned() - heldBack;
            if (backToStock > 0) {
                links.addToAvailableStock(asInt(item.get("equipment_id")), backToStock);
            }

            // INTEGRATION (Module 4 -> Module 3): late return charge
            BigDecimal late = line.lateCharge() != null
                    ? money(line.lateCharge())
                    : Module4Support.lateChargePerUnit(
                            (BigDecimal) item.get("rate_per_unit"),
                            (String) item.get("rate_period"),
                            daysLate).multiply(BigDecimal.valueOf(line.quantityReturned()));
            if (Module4Support.isPositive(late)) {
                billing.addCharge(rentalId, line.rentalItemId(), Module4Billing.LATE,
                        "Late return: " + name + " x" + line.quantityReturned() + ", "
                                + daysLate + " day(s) after " + due,
                        late, userId);
                lateTotal = lateTotal.add(money(late));
            }
        }

        // ---- 3. lost units ----
        for (LostLine line : lostLines) {
            LostItem lost = lostItemService.save(rentalId, items.get(line.rentalItemId()), line, userId);
            unitsLost += lost.getQuantityLost();
            lostTotal = lostTotal.add(money(lost.getChargeAmount()));
        }

        // ---- 4. statuses ----
        returnItemRepository.flush();
        String rentalStatus = support.refreshStatuses(rentalId);
        String returnType = "RETURNED".equals(rentalStatus) ? "FULL" : "PARTIAL";
        if (rentalReturn != null) {
            rentalReturn.setReturnType(returnType);
            rentalReturnRepository.saveAndFlush(rentalReturn);
        }

        return new ProcessReturnResponse(
                rentalReturn == null ? null : rentalReturn.getReturnId(),
                rentalId,
                returnType,
                rentalStatus,
                unitsReturned,
                unitsLost,
                money(lateTotal),
                money(damageTotal),
                money(lostTotal),
                "RETURNED".equals(rentalStatus));
    }

    private static Map<String, Object> requireItem(Map<Integer, Map<String, Object>> items,
                                                   Integer rentalItemId, Integer rentalId) {
        Map<String, Object> item = rentalItemId == null ? null : items.get(rentalItemId);
        if (item == null) {
            throw new IllegalArgumentException("Rental item #" + rentalItemId
                    + " is not an issued item of rental #" + rentalId + ".");
        }
        return item;
    }

    private static String normaliseCondition(String condition) {
        String c = condition == null ? "" : condition.trim().toUpperCase().replace('_', ' ');
        if (!Module4Support.CONDITIONS.contains(c)) {
            throw new IllegalArgumentException("Condition must be GOOD, DAMAGED, MISSING PARTS or NEEDS MAINTENANCE.");
        }
        return c;
    }

    /** Accepts "2026-10-07T15:30" / "2026-10-07T15:30:00" / "2026-10-07"; empty = now. */
    private static LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return value.length() == 10
                    ? LocalDate.parse(value).atTime(LocalDateTime.now().toLocalTime())
                    : LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Return date must look like 2026-10-07T15:30.");
        }
    }
}
