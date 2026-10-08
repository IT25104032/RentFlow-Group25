package com.compulin.rentflow.service.module4;

import com.compulin.rentflow.dto.module4.SettlementDtos.ChargeLine;
import com.compulin.rentflow.dto.module4.SettlementDtos.SettlementPreview;
import com.compulin.rentflow.dto.module4.SettlementDtos.SettlementRow;
import com.compulin.rentflow.entity.module4.LostItem;
import com.compulin.rentflow.entity.module4.Settlement;
import com.compulin.rentflow.repository.module4.LostItemRepository;
import com.compulin.rentflow.repository.module4.RentalLookupRepository;
import com.compulin.rentflow.repository.module4.SettlementRepository;
import com.compulin.rentflow.service.module4.Module4Links.DepositRow;
import com.compulin.rentflow.service.module4.Module4Links.InvoiceRow;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.compulin.rentflow.service.module4.Module4Support.asInt;
import static com.compulin.rentflow.service.module4.Module4Support.money;

/*
 * MODULE 4 (IT25104066) - final settlement and closing the rental.
 *
 * Settle (only when every issued unit is returned or recorded as lost):
 *   1. make sure every charge is on the rental's invoice (Module 3 table)
 *   2. use the held security deposit to pay the invoice balance
 *      (recorded as a CASH payment with reference DEPOSIT-ADJUSTMENT,
 *       because payment_method only allows CASH / CARD / BANK_TRANSFER)
 *   3. refund what is left of the deposit
 *   4. save the settlement row
 *   5. if nothing is owed: SETTLED, rental CLOSED, lost notes SETTLED
 *      otherwise: PENDING until the rentee pays the rest
 */
@Service
public class SettlementService {

    public static final String DEPOSIT_REFERENCE = "DEPOSIT-ADJUSTMENT";

    private final SettlementRepository settlementRepository;
    private final LostItemRepository lostItemRepository;
    private final RentalLookupRepository lookup;
    private final Module4Support support;

    // invoice, payment and deposit (Module 3 tables)
    private final Module4Links links;

    public SettlementService(SettlementRepository settlementRepository,
                             LostItemRepository lostItemRepository,
                             RentalLookupRepository lookup,
                             Module4Support support,
                             Module4Links links) {
        this.settlementRepository = settlementRepository;
        this.lostItemRepository = lostItemRepository;
        this.lookup = lookup;
        this.support = support;
        this.links = links;
    }

    public List<SettlementRow> list(Integer companyId, String search) {
        return lookup.findSettlementRows(companyId, search);
    }

    /** What settling would do now. Nothing is saved. */
    @Transactional(readOnly = true)
    public SettlementPreview preview(Integer companyId, Integer rentalId) {
        Map<String, Object> rental = support.rental(rentalId, companyId);
        List<ChargeLine> charges = lookup.findCharges(rentalId);

        BigDecimal rentalCharges = sum(charges, "RENTAL", "EXTENSION", "OTHER");
        BigDecimal late = sum(charges, "LATE");
        BigDecimal damage = sum(charges, "DAMAGE");
        BigDecimal lost = sum(charges, "LOST_ITEM");
        BigDecimal total = rentalCharges.add(late).add(damage).add(lost);

        InvoiceRow invoice = links.openInvoice(rentalId);
        BigDecimal paid = invoice == null ? BigDecimal.ZERO : invoice.amountPaid();
        BigDecimal outstanding = total.subtract(paid);

        DepositRow deposit = links.deposit(rentalId);
        BigDecimal held = heldAmount(deposit);
        BigDecimal toUse = outstanding.max(BigDecimal.ZERO).min(held);

        int unitsOut = asInt(rental.get("units_out"));
        boolean resolved = unitsOut == 0 && !lookup.findIssuedItems(rentalId).isEmpty();
        Settlement s = settlementRepository.findByRentalId(rentalId).orElse(null);

        return new SettlementPreview(
                rentalId,
                (String) rental.get("customer_name"),
                (String) rental.get("phone"),
                rental.get("start_date").toString(),
                rental.get("due_date").toString(),
                (String) rental.get("rental_status"),
                unitsOut,
                resolved,
                invoice == null ? null : invoice.invoiceId(),
                money(rentalCharges),
                money(late),
                money(damage),
                money(lost),
                money(total),
                paid,
                money(outstanding),
                deposit == null ? null : deposit.status(),
                deposit == null ? lookup.calculatedDeposit(rentalId) : deposit.calculatedDeposit(),
                money(held),
                money(toUse),
                money(held.subtract(toUse)),
                money(outstanding.subtract(toUse)),
                s == null ? null : s.getSettlementStatus(),
                s == null || s.getSettledAt() == null ? null : s.getSettledAt().toString(),
                s == null ? null : money(s.getDepositUsed()),
                s == null ? null : money(s.getDepositRefunded()),
                s == null ? null : money(s.getFinalBalance()),
                charges,
                lookup.findReturns(companyId, null, rentalId, null));
    }

    @Transactional
    public SettlementPreview settle(Integer companyId, Integer rentalId, Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Please log in before settling a rental.");
        }
        Map<String, Object> rental = support.rental(rentalId, companyId);
        int unitsOut = asInt(rental.get("units_out"));
        if (unitsOut > 0) {
            throw new IllegalArgumentException(unitsOut + " unit(s) are still out. Return them or record them "
                    + "as lost before settling.");
        }
        if (lookup.findIssuedItems(rentalId).isEmpty()) {
            throw new IllegalArgumentException("Nothing was issued on rental #" + rentalId + ", so there is nothing to settle.");
        }
        Settlement settlement = settlementRepository.findByRentalId(rentalId).orElseGet(Settlement::new);
        if ("SETTLED".equals(settlement.getSettlementStatus())) {
            throw new IllegalArgumentException("Rental #" + rentalId + " is already settled.");
        }

        // 1. every charge on the invoice
        InvoiceRow invoice = billEverything(rentalId);

        // 2-3. deposit: pay the balance with it, refund the rest
        DepositRow deposit = links.deposit(rentalId);
        BigDecimal held = heldAmount(deposit);
        BigDecimal depositUsed = deposit == null ? BigDecimal.ZERO : deposit.amountDeducted();
        BigDecimal depositRefunded = deposit == null ? BigDecimal.ZERO : deposit.amountRefunded();
        if (deposit != null && held.signum() > 0) {
            BigDecimal balance = invoice == null ? BigDecimal.ZERO : invoice.balanceDue().max(BigDecimal.ZERO);
            BigDecimal use = balance.min(held);
            if (use.signum() > 0) {
                links.recordPayment(invoice.invoiceId(), use, "CASH", DEPOSIT_REFERENCE, userId);
            }
            BigDecimal refund = held.subtract(use);
            depositUsed = depositUsed.add(use);
            depositRefunded = depositRefunded.add(refund);
            String status = use.signum() == 0 ? "REFUNDED"
                    : refund.signum() == 0 ? "FORFEITED"
                    : "PARTIALLY_REFUNDED";
            links.closeDeposit(deposit.depositId(), depositUsed, depositRefunded, status);
        }

        // 4. the settlement snapshot
        List<ChargeLine> charges = lookup.findCharges(rentalId);
        BigDecimal rentalCharges = sum(charges, "RENTAL", "EXTENSION", "OTHER");
        BigDecimal late = sum(charges, "LATE");
        BigDecimal damage = sum(charges, "DAMAGE");
        BigDecimal lost = sum(charges, "LOST_ITEM");
        InvoiceRow latest = links.openInvoice(rentalId);
        BigDecimal finalBalance = latest == null ? BigDecimal.ZERO : latest.balanceDue();

        settlement.setRentalId(rentalId);
        settlement.setRentalCharges(money(rentalCharges.max(BigDecimal.ZERO)));
        settlement.setLateCharges(money(late.max(BigDecimal.ZERO)));
        settlement.setDamageCharges(money(damage.max(BigDecimal.ZERO)));
        settlement.setLostItemCharges(money(lost.max(BigDecimal.ZERO)));
        settlement.setTotalCharges(money(rentalCharges.add(late).add(damage).add(lost).max(BigDecimal.ZERO)));
        settlement.setDepositUsed(money(depositUsed));
        settlement.setDepositRefunded(money(depositRefunded));
        settlement.setFinalBalance(finalBalance);
        settlement.setSettledBy(userId);

        // 5. close when nothing is owed
        if (finalBalance.signum() <= 0) {
            settlement.setSettlementStatus("SETTLED");
            settlement.setSettledAt(LocalDateTime.now());
            List<Integer> lineIds = lookup.findIssuedItems(rentalId).stream()
                    .map(i -> asInt(i.get("rental_item_id"))).toList();
            for (LostItem l : lostItemRepository.findByRentalItemIdIn(lineIds)) {
                if (List.of("PENDING", "CHARGED").contains(l.getLostStatus())) {
                    l.setLostStatus("SETTLED");
                }
            }
            lostItemRepository.flush();
            lookup.closeRentalItems(rentalId);
            lookup.updateRentalStatus(rentalId, "CLOSED");
        } else {
            settlement.setSettlementStatus("PENDING");
            settlement.setSettledAt(null);
        }
        settlementRepository.saveAndFlush(settlement);
        return preview(companyId, rentalId);
    }

    /** Collects (part of) the remaining balance on the rental's invoice, then settles again. */
    @Transactional
    public SettlementPreview payAndSettle(Integer companyId, Integer rentalId, BigDecimal amount,
                                          String method, String referenceNo, Integer userId) {
        support.rental(rentalId, companyId);
        if (userId == null) {
            throw new IllegalArgumentException("Please log in before recording a payment.");
        }
        if (!Module4Support.isPositive(amount)) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
        String paymentMethod = method == null ? "CASH" : method.trim().toUpperCase();
        if (!List.of("CASH", "CARD", "BANK_TRANSFER").contains(paymentMethod)) {
            throw new IllegalArgumentException("Payment method must be CASH, CARD or BANK_TRANSFER.");
        }
        InvoiceRow invoice = billEverything(rentalId);
        if (invoice == null) {
            throw new IllegalArgumentException("Rental #" + rentalId + " has no invoice to pay.");
        }
        links.recordPayment(invoice.invoiceId(), money(amount), paymentMethod,
                Module4Support.blankToNull(referenceNo), userId);
        return settle(companyId, rentalId, userId);
    }

    /** Puts every charge of the rental on its invoice (creating the invoice if needed). */
    private InvoiceRow billEverything(Integer rentalId) {
        InvoiceRow invoice = links.openInvoice(rentalId);
        if (invoice != null) {
            return links.attachUnbilledCharges(rentalId, invoice.invoiceId());
        }
        if (!links.hasCharges(rentalId)) {
            return null;
        }
        if (links.invoice(rentalId) != null) {
            throw new IllegalArgumentException("The invoice of rental #" + rentalId
                    + " was cancelled. Ask billing (Module 3) to reissue it before settling.");
        }
        return links.createInvoice(rentalId);
    }

    /** Deposit still held for the rentee (received minus already deducted / refunded). */
    private static BigDecimal heldAmount(DepositRow deposit) {
        if (deposit == null || !"HELD".equals(deposit.status())) {
            return BigDecimal.ZERO;
        }
        return deposit.amountReceived()
                .subtract(deposit.amountDeducted())
                .subtract(deposit.amountRefunded())
                .max(BigDecimal.ZERO);
    }

    private static BigDecimal sum(List<ChargeLine> charges, String... types) {
        BigDecimal total = BigDecimal.ZERO;
        for (ChargeLine c : charges) {
            for (String t : types) {
                if (t.equals(c.chargeType())) {
                    total = total.add(c.amount());
                }
            }
        }
        return total;
    }
}
