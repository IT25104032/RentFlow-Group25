import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

import { getOpenRentals, getRentalForReturn, processReturn } from "../../services/module4/module4Api";
import {
    Alert, Badge, Card, Empty, Field, Loading, Stepper, date, money, nowLocal
} from "../../components/module4/m4ui";

// =========================================================
// MODULE 4 - Process Return, as a 4-step wizard:
//   1 Find rental -> 2 Check items -> 3 Review -> 4 Done
// A rental line can be split (2 good + 1 damaged), damaged units
// get a damage assessment, and missing units a lost note, all
// saved together in one request.
// =========================================================

const STEPS = ["Find rental", "Check items", "Review & confirm", "Done"];
const CONDITIONS = ["GOOD", "DAMAGED", "MISSING PARTS", "NEEDS MAINTENANCE"];
const CONDITION_LABEL = {
    GOOD: "Good", DAMAGED: "Damaged", "MISSING PARTS": "Missing parts", "NEEDS MAINTENANCE": "Needs maintenance"
};
const LOSS_TYPES = [["LOST", "Lost"], ["STOLEN", "Stolen"], ["NON_RETURNED", "Not returned"]];

let lineKey = 0;
const newLine = (rentalItemId, qty, condition = "GOOD") => ({
    key: ++lineKey,
    rentalItemId,
    qty,
    condition,
    notes: "",
    lateCharge: null, // null = use the calculated amount
    damage: { level: "MINOR", description: "", estimatedCost: "", finalCharge: "", damagedQty: "" }
});

const num = (v) => (v === "" || v === null || v === undefined ? 0 : Number(v));

function ProcessReturn() {
    const navigate = useNavigate();
    const [params, setParams] = useSearchParams();

    const [step, setStep] = useState(0);
    const [error, setError] = useState("");

    // step 1
    const [search, setSearch] = useState("");
    const [rentals, setRentals] = useState(null);

    // step 2
    const [rental, setRental] = useState(null);
    const [returnDate, setReturnDate] = useState(nowLocal());
    const [lines, setLines] = useState([]);
    const [lost, setLost] = useState({});
    const [notes, setNotes] = useState("");

    // step 4
    const [saving, setSaving] = useState(false);
    const [result, setResult] = useState(null);

    // ---------- step 1: rentals with equipment out ----------
    useEffect(() => {
        if (step !== 0) return;
        const t = setTimeout(() => {
            getOpenRentals(search.trim())
                .then(setRentals)
                .catch((err) => setError(err.message));
        }, 250);
        return () => clearTimeout(t);
    }, [search, step]);

    const openRental = useCallback(async (rentalId) => {
        setError("");
        try {
            const data = await getRentalForReturn(rentalId, returnDate);
            setRental(data);
            // assume everything still out comes back in good condition; staff adjust from there
            setLines(data.items.filter((i) => i.remainingQuantity > 0)
                .map((i) => newLine(i.rentalItemId, i.remainingQuantity)));
            setLost({});
            setNotes("");
            setStep(1);
            setParams({ rentalId: String(rentalId) }, { replace: true });
        } catch (err) {
            setError(err.message);
        }
    }, [returnDate, setParams]);

    // opened from the overview with ?rentalId=
    useEffect(() => {
        const id = params.get("rentalId");
        if (id && !rental && step === 0) {
            openRental(id);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // a different return date changes the late charge
    async function changeReturnDate(value) {
        setReturnDate(value);
        if (!rental || !value) return;
        try {
            const data = await getRentalForReturn(rental.rentalId, value);
            setRental(data);
        } catch (err) {
            setError(err.message);
        }
    }

    // ---------- step 2 helpers ----------
    const itemById = useMemo(() => {
        const map = {};
        (rental?.items || []).forEach((i) => { map[i.rentalItemId] = i; });
        return map;
    }, [rental]);

    function lateFor(line) {
        if (line.lateCharge !== null) return num(line.lateCharge);
        const item = itemById[line.rentalItemId];
        return item ? num(item.lateChargePerUnit) * num(line.qty) : 0;
    }

    const updateLine = (key, patch) =>
        setLines((prev) => prev.map((l) => (l.key === key ? { ...l, ...patch } : l)));

    const updateDamage = (key, patch) =>
        setLines((prev) => prev.map((l) => (l.key === key ? { ...l, damage: { ...l.damage, ...patch } } : l)));

    const removeLine = (key) => setLines((prev) => prev.filter((l) => l.key !== key));

    function addSplit(item) {
        const used = usedFor(item.rentalItemId);
        const left = Math.max(1, item.remainingQuantity - used);
        setLines((prev) => [...prev, newLine(item.rentalItemId, Math.min(left, item.remainingQuantity), "DAMAGED")]);
    }

    function returnAllGood() {
        setLines(rental.items.filter((i) => i.remainingQuantity > 0)
            .map((i) => newLine(i.rentalItemId, i.remainingQuantity)));
        setLost({});
    }

    function skipItem(rentalItemId) {
        setLines((prev) => prev.filter((l) => l.rentalItemId !== rentalItemId));
        setLost((prev) => {
            const next = { ...prev };
            delete next[rentalItemId];
            return next;
        });
    }

    function toggleLost(item) {
        setLost((prev) => {
            const next = { ...prev };
            if (next[item.rentalItemId]) {
                delete next[item.rentalItemId];
            } else {
                const left = Math.max(1, item.remainingQuantity - linesQty(item.rentalItemId));
                next[item.rentalItemId] = { qty: left, lossType: "NON_RETURNED", cost: "", reason: "" };
            }
            return next;
        });
    }

    const updateLost = (rentalItemId, patch) =>
        setLost((prev) => ({ ...prev, [rentalItemId]: { ...prev[rentalItemId], ...patch } }));

    function linesQty(rentalItemId) {
        return lines.filter((l) => l.rentalItemId === rentalItemId).reduce((s, l) => s + num(l.qty), 0);
    }

    function usedFor(rentalItemId) {
        return linesQty(rentalItemId) + num(lost[rentalItemId]?.qty);
    }

    // problems per rental line, shown on the card
    function problemsFor(item) {
        const out = [];
        const itemLines = lines.filter((l) => l.rentalItemId === item.rentalItemId);
        itemLines.forEach((l) => {
            if (num(l.qty) <= 0) out.push("Quantity must be at least 1 (or remove the line).");
            if (l.condition !== "GOOD") {
                if (!l.damage.description.trim()) out.push(`Describe the ${CONDITION_LABEL[l.condition].toLowerCase()} units.`);
                if (l.damage.damagedQty !== "" && (num(l.damage.damagedQty) < 1 || num(l.damage.damagedQty) > num(l.qty))) {
                    out.push("Damaged quantity must be between 1 and the quantity on that line.");
                }
            }
            if (l.lateCharge !== null && num(l.lateCharge) < 0) out.push("Late charge cannot be negative.");
        });
        const lostLine = lost[item.rentalItemId];
        if (lostLine && num(lostLine.qty) <= 0) out.push("Lost quantity must be at least 1.");
        if (usedFor(item.rentalItemId) > item.remainingQuantity) {
            out.push(`Only ${item.remainingQuantity} unit(s) are out; returned + lost is ${usedFor(item.rentalItemId)}.`);
        }
        return out;
    }

    const outItems = (rental?.items || []).filter((i) => i.remainingQuantity > 0);
    const allProblems = outItems.flatMap(problemsFor);
    const unitsReturning = lines.reduce((s, l) => s + num(l.qty), 0);
    const unitsLost = Object.values(lost).reduce((s, l) => s + num(l.qty), 0);
    const totalOut = outItems.reduce((s, i) => s + i.remainingQuantity, 0);
    const lateTotal = lines.reduce((s, l) => s + lateFor(l), 0);
    const damageTotal = lines.filter((l) => l.condition !== "GOOD").reduce((s, l) => s + num(l.damage.finalCharge), 0);
    const lostTotal = Object.values(lost).reduce((s, l) => s + num(l.qty) * num(l.cost), 0);
    const stillOutAfter = totalOut - unitsReturning - unitsLost;
    const canReview = unitsReturning + unitsLost > 0 && allProblems.length === 0;

    // ---------- step 3: save ----------
    async function confirm() {
        setError("");
        setSaving(true);
        try {
            const payload = {
                rentalId: rental.rentalId,
                returnDate,
                notes,
                items: lines.map((l) => ({
                    rentalItemId: l.rentalItemId,
                    quantityReturned: num(l.qty),
                    conditionStatus: l.condition,
                    inspectionNotes: l.notes,
                    lateCharge: Number(lateFor(l).toFixed(2)),
                    damage: l.condition === "GOOD" ? null : {
                        damagedQuantity: l.damage.damagedQty === "" ? num(l.qty) : num(l.damage.damagedQty),
                        damageLevel: l.damage.level,
                        damageDescription: l.damage.description,
                        estimatedCost: num(l.damage.estimatedCost),
                        finalCharge: num(l.damage.finalCharge)
                    }
                })),
                lostItems: Object.entries(lost).map(([rentalItemId, l]) => ({
                    rentalItemId: Number(rentalItemId),
                    quantityLost: num(l.qty),
                    lossType: l.lossType,
                    replacementCostPerUnit: num(l.cost),
                    reason: l.reason
                }))
            };
            const saved = await processReturn(payload);
            setResult(saved);
            setStep(3);
            window.scrollTo({ top: 0, behavior: "smooth" });
        } catch (err) {
            setError(err.message);
        } finally {
            setSaving(false);
        }
    }

    function startOver() {
        setRental(null);
        setLines([]);
        setLost({});
        setResult(null);
        setSearch("");
        setReturnDate(nowLocal());
        setParams({}, { replace: true });
        setStep(0);
    }

    // =========================================================
    // render
    // =========================================================
    return (
        <>
            <Stepper steps={STEPS} current={step} onStep={step < 3 ? (i) => (i === 0 ? startOver() : setStep(i)) : null} />
            <Alert onClose={() => setError("")}>{error}</Alert>

            {step === 0 && (
                <Card title="Which rental is coming back?" flush>
                    <div className="m4-card-body" style={{ paddingBottom: 0 }}>
                        <div className="m4-search">
                            <input className="m4-input" autoFocus placeholder="Search by rental number, renter name or phone"
                                   value={search} onChange={(e) => setSearch(e.target.value)} />
                        </div>
                    </div>
                    {!rentals ? <Loading /> : rentals.length === 0 ? (
                        <Empty>{search ? "No rental with equipment out matches that search." : "No equipment is out right now."}</Empty>
                    ) : (
                        <div className="m4-table-wrap">
                            <table className="m4-table">
                                <thead>
                                <tr><th>Rental</th><th>Renter</th><th>Period</th><th>Status</th><th className="m4-right">Units out</th><th></th></tr>
                                </thead>
                                <tbody>
                                {rentals.map((r) => (
                                    <tr key={r.rentalId} className="m4-row-link" onClick={() => openRental(r.rentalId)}>
                                        <td><strong>#{r.rentalId}</strong></td>
                                        <td>{r.customerName}<span className="m4-sub">{r.customerPhone}</span></td>
                                        <td className="m4-nowrap">{date(r.startDate)} → {date(r.dueDate)}</td>
                                        <td>
                                            {r.daysOverdue > 0
                                                ? <Badge value="OVERDUE">Overdue {r.daysOverdue}d</Badge>
                                                : <Badge value={r.rentalStatus} />}
                                        </td>
                                        <td className="m4-right">{r.unitsOut}</td>
                                        <td className="m4-right"><button className="m4-btn m4-btn-primary m4-btn-sm">Select</button></td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </Card>
            )}

            {step >= 1 && step <= 2 && rental && (
                <div className="m4-rental-strip">
                    <div className="m4-kv"><span>Rental</span><strong>#{rental.rentalId}</strong></div>
                    <div className="m4-kv"><span>Renter</span><strong>{rental.customerName}</strong></div>
                    <div className="m4-kv"><span>Phone</span><strong>{rental.customerPhone || "-"}</strong></div>
                    <div className="m4-kv"><span>Period</span><strong>{date(rental.startDate)} → {date(rental.dueDate)}</strong></div>
                    <div className="m4-kv">
                        <span>Return</span>
                        {rental.daysLate > 0
                            ? <Badge value="OVERDUE">{rental.daysLate} day(s) late</Badge>
                            : <Badge value="RETURNED">On time</Badge>}
                    </div>
                </div>
            )}

            {step === 1 && rental && (
                <>
                    <Card>
                        <div className="m4-form-row">
                            <Field label="Actual return date & time"
                                   hint={rental.daysLate > 0 ? "Late charge is worked out per day after the due date. You can change it per line." : "Not late, no late charge."}>
                                <input type="datetime-local" className="m4-input" value={returnDate}
                                       max={nowLocal()} onChange={(e) => changeReturnDate(e.target.value)} />
                            </Field>
                            <div style={{ display: "flex", alignItems: "flex-end", gap: 8 }}>
                                <button className="m4-btn" onClick={returnAllGood}>↺ Everything back in good condition</button>
                            </div>
                        </div>
                    </Card>

                    {rental.items.map((item) => {
                        const itemLines = lines.filter((l) => l.rentalItemId === item.rentalItemId);
                        const lostLine = lost[item.rentalItemId];
                        const problems = item.remainingQuantity > 0 ? problemsFor(item) : [];
                        const now = linesQty(item.rentalItemId);
                        const pct = (n) => `${Math.min(100, (n / Math.max(1, item.issuedQuantity)) * 100)}%`;
                        const selected = now + num(lostLine?.qty) > 0;

                        return (
                            <div key={item.rentalItemId}
                                 className={`m4-item ${problems.length ? "m4-item-error" : selected ? "m4-item-selected" : ""}`}>
                                <div className="m4-item-head">
                                    <div>
                                        <h3>{item.equipmentName}</h3>
                                        <span className="m4-muted m4-small">
                                            {item.itemCode} · {money(item.ratePerUnit)} / {String(item.ratePeriod || "").toLowerCase()}
                                            {num(item.lateChargePerUnit) > 0 && <> · late {money(item.lateChargePerUnit)} per unit</>}
                                        </span>
                                    </div>
                                    <div className="m4-item-progress">
                                        <div className="m4-meter" title="returned before / now / lost">
                                            <span className="m4-meter-done" style={{ width: pct(item.returnedQuantity + item.lostQuantity) }} />
                                            <span className="m4-meter-now" style={{ width: pct(now) }} />
                                            <span className="m4-meter-lost" style={{ width: pct(num(lostLine?.qty)) }} />
                                        </div>
                                        <span>
                                            {item.remainingQuantity === 0
                                                ? "All back"
                                                : <><strong>{item.remainingQuantity}</strong> of {item.issuedQuantity} still out</>}
                                        </span>
                                    </div>
                                </div>

                                {item.remainingQuantity === 0 ? (
                                    <div className="m4-item-out">
                                        Already resolved: {item.returnedQuantity} returned{item.lostQuantity > 0 && `, ${item.lostQuantity} recorded lost`}.
                                    </div>
                                ) : (
                                    <>
                                        {itemLines.map((line) => (
                                            <div className="m4-line" key={line.key}>
                                                <div className="m4-line-grid">
                                                    <Field label="Qty returned">
                                                        <input type="number" min="1" max={item.remainingQuantity} className="m4-input"
                                                               value={line.qty}
                                                               onChange={(e) => updateLine(line.key, { qty: e.target.value })} />
                                                    </Field>
                                                    <Field label="Condition">
                                                        <select className="m4-select" value={line.condition}
                                                                onChange={(e) => updateLine(line.key, { condition: e.target.value })}>
                                                            {CONDITIONS.map((c) => <option key={c} value={c}>{CONDITION_LABEL[c]}</option>)}
                                                        </select>
                                                    </Field>
                                                    <Field label="Late charge (Rs.)"
                                                           hint={line.lateCharge === null ? (rental.daysLate > 0 ? "calculated" : "not late") : (
                                                               <button type="button" className="m4-link" onClick={() => updateLine(line.key, { lateCharge: null })}>use calculated</button>)}>
                                                        <input type="number" min="0" step="0.01" className="m4-input"
                                                               value={line.lateCharge === null ? lateFor(line).toFixed(2) : line.lateCharge}
                                                               onChange={(e) => updateLine(line.key, { lateCharge: e.target.value })} />
                                                    </Field>
                                                    <Field label="Inspection notes">
                                                        <input className="m4-input" value={line.notes} placeholder="optional"
                                                               onChange={(e) => updateLine(line.key, { notes: e.target.value })} />
                                                    </Field>
                                                    <button type="button" className="m4-remove" title="Remove this line"
                                                            onClick={() => removeLine(line.key)}>×</button>
                                                </div>

                                                {line.condition !== "GOOD" && (
                                                    <div className="m4-subpanel m4-subpanel-damage">
                                                        <h4>Damage assessment · these units stay out of stock until marked repaired</h4>
                                                        <div className="m4-form-row">
                                                            <Field label="What is wrong? *">
                                                                <input className="m4-input" value={line.damage.description}
                                                                       placeholder="e.g. chuck cracked"
                                                                       onChange={(e) => updateDamage(line.key, { description: e.target.value })} />
                                                            </Field>
                                                            <Field label="Level">
                                                                <select className="m4-select" value={line.damage.level}
                                                                        onChange={(e) => updateDamage(line.key, { level: e.target.value })}>
                                                                    <option value="MINOR">Minor</option>
                                                                    <option value="MODERATE">Moderate</option>
                                                                    <option value="SEVERE">Severe</option>
                                                                </select>
                                                            </Field>
                                                            <Field label="Units affected" hint={`default: all ${num(line.qty)}`}>
                                                                <input type="number" min="1" max={num(line.qty)} className="m4-input"
                                                                       value={line.damage.damagedQty} placeholder={String(num(line.qty))}
                                                                       onChange={(e) => updateDamage(line.key, { damagedQty: e.target.value })} />
                                                            </Field>
                                                            <Field label="Estimated repair (Rs.)">
                                                                <input type="number" min="0" className="m4-input" value={line.damage.estimatedCost}
                                                                       onChange={(e) => updateDamage(line.key, { estimatedCost: e.target.value })} />
                                                            </Field>
                                                            <Field label="Charge to renter (Rs.)" hint="0 = assess now, charge later">
                                                                <input type="number" min="0" className="m4-input" value={line.damage.finalCharge}
                                                                       onChange={(e) => updateDamage(line.key, { finalCharge: e.target.value })} />
                                                            </Field>
                                                        </div>
                                                    </div>
                                                )}
                                            </div>
                                        ))}

                                        {lostLine && (
                                            <div className="m4-line">
                                                <div className="m4-subpanel m4-subpanel-lost" style={{ marginTop: 0 }}>
                                                    <h4>Lost note · these units leave the company's stock</h4>
                                                    <div className="m4-form-row">
                                                        <Field label="Units lost">
                                                            <input type="number" min="1" className="m4-input" value={lostLine.qty}
                                                                   onChange={(e) => updateLost(item.rentalItemId, { qty: e.target.value })} />
                                                        </Field>
                                                        <Field label="Type">
                                                            <select className="m4-select" value={lostLine.lossType}
                                                                    onChange={(e) => updateLost(item.rentalItemId, { lossType: e.target.value })}>
                                                                {LOSS_TYPES.map(([v, t]) => <option key={v} value={v}>{t}</option>)}
                                                            </select>
                                                        </Field>
                                                        <Field label="Replacement cost per unit (Rs.)" hint="0 = record now, charge later">
                                                            <input type="number" min="0" className="m4-input" value={lostLine.cost}
                                                                   onChange={(e) => updateLost(item.rentalItemId, { cost: e.target.value })} />
                                                        </Field>
                                                        <Field label="Reason">
                                                            <input className="m4-input" value={lostLine.reason} placeholder="optional"
                                                                   onChange={(e) => updateLost(item.rentalItemId, { reason: e.target.value })} />
                                                        </Field>
                                                    </div>
                                                </div>
                                            </div>
                                        )}

                                        {problems.length > 0 && (
                                            <div className="m4-line"><Alert>{problems.join(" ")}</Alert></div>
                                        )}

                                        <div className="m4-item-foot">
                                            {itemLines.length === 0 ? (
                                                <button className="m4-link" onClick={() => setLines((p) => [...p, newLine(item.rentalItemId, Math.max(1, item.remainingQuantity - num(lostLine?.qty)))])}>
                                                    + Return this item
                                                </button>
                                            ) : (
                                                <button className="m4-link" onClick={() => addSplit(item)}>+ Split: some units in a different condition</button>
                                            )}
                                            <button className="m4-link" onClick={() => toggleLost(item)}>
                                                {lostLine ? "− Remove lost note" : "+ Some units did not come back"}
                                            </button>
                                            {selected && (
                                                <button className="m4-link m4-link-danger" onClick={() => skipItem(item.rentalItemId)}>Not returning this item now</button>
                                            )}
                                        </div>
                                    </>
                                )}
                            </div>
                        );
                    })}

                    <div className="m4-actionbar">
                        <div className="m4-actionbar-summary">
                            <span>Returning <strong>{unitsReturning}</strong> of {totalOut}</span>
                            {unitsLost > 0 && <span>Lost <strong>{unitsLost}</strong></span>}
                            {lateTotal > 0 && <span>Late <strong>{money(lateTotal)}</strong></span>}
                            {damageTotal > 0 && <span>Damage <strong>{money(damageTotal)}</strong></span>}
                            {lostTotal > 0 && <span>Lost <strong>{money(lostTotal)}</strong></span>}
                        </div>
                        <div className="m4-actionbar-buttons">
                            <button className="m4-btn" onClick={startOver}>← Change rental</button>
                            <button className="m4-btn m4-btn-primary" disabled={!canReview}
                                    onClick={() => { setStep(2); window.scrollTo({ top: 0, behavior: "smooth" }); }}>
                                Review return →
                            </button>
                        </div>
                    </div>
                </>
            )}

            {step === 2 && rental && (
                <>
                    <Card title="What is coming back" flush>
                        <div className="m4-table-wrap">
                            <table className="m4-table">
                                <thead>
                                <tr><th>Item</th><th className="m4-right">Qty</th><th>Condition</th><th>Notes / damage</th><th className="m4-right">Late</th><th className="m4-right">Damage</th></tr>
                                </thead>
                                <tbody>
                                {lines.map((l) => (
                                    <tr key={l.key}>
                                        <td>{itemById[l.rentalItemId]?.equipmentName}</td>
                                        <td className="m4-right">{num(l.qty)}</td>
                                        <td><Badge value={l.condition}>{CONDITION_LABEL[l.condition]}</Badge></td>
                                        <td>
                                            {l.condition !== "GOOD"
                                                ? <>{l.damage.description} <span className="m4-sub">{l.damage.level.toLowerCase()} · {l.damage.damagedQty || l.qty} unit(s) held for repair</span></>
                                                : (l.notes || <span className="m4-muted">-</span>)}
                                        </td>
                                        <td className="m4-right">{lateFor(l) > 0 ? money(lateFor(l)) : "-"}</td>
                                        <td className="m4-right">{l.condition !== "GOOD" && num(l.damage.finalCharge) > 0 ? money(l.damage.finalCharge) : "-"}</td>
                                    </tr>
                                ))}
                                {Object.entries(lost).map(([id, l]) => (
                                    <tr key={"lost" + id}>
                                        <td>{itemById[id]?.equipmentName}</td>
                                        <td className="m4-right">{num(l.qty)}</td>
                                        <td><Badge value={l.lossType} /></td>
                                        <td>{l.reason || <span className="m4-muted">lost note</span>}</td>
                                        <td className="m4-right">-</td>
                                        <td className="m4-right">{num(l.cost) > 0 ? money(num(l.cost) * num(l.qty)) : "-"}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </Card>

                    <div className="m4-grid-2">
                        <Card title="Added to the bill">
                            <div className="m4-breakdown">
                                <div className="m4-breakdown-row"><span>Late return charges</span><span>{money(lateTotal)}</span></div>
                                <div className="m4-breakdown-row"><span>Damage charges</span><span>{money(damageTotal)}</span></div>
                                <div className="m4-breakdown-row"><span>Lost item charges</span><span>{money(lostTotal)}</span></div>
                                <div className="m4-breakdown-row total"><span>Total new charges</span><span>{money(lateTotal + damageTotal + lostTotal)}</span></div>
                            </div>
                        </Card>
                        <Card title="After this return">
                            {stillOutAfter <= 0 ? (
                                <Alert type="success">Everything is back. Rental #{rental.rentalId} will be marked <strong>Returned</strong> and is ready for settlement.</Alert>
                            ) : (
                                <Alert type="info">Partial return. {stillOutAfter} unit(s) will still be out with {rental.customerName}.</Alert>
                            )}
                            <Field label="Return notes">
                                <textarea className="m4-textarea" value={notes} placeholder="optional, e.g. who brought it back"
                                          onChange={(e) => setNotes(e.target.value)} />
                            </Field>
                        </Card>
                    </div>

                    <div className="m4-actionbar">
                        <div className="m4-actionbar-summary">
                            <span>Return date <strong>{returnDate.replace("T", " ")}</strong></span>
                        </div>
                        <div className="m4-actionbar-buttons">
                            <button className="m4-btn" onClick={() => setStep(1)}>← Back to items</button>
                            <button className="m4-btn m4-btn-primary m4-btn-lg" disabled={saving} onClick={confirm}>
                                {saving ? "Saving..." : "Confirm return"}
                            </button>
                        </div>
                    </div>
                </>
            )}

            {step === 3 && result && (
                <Card>
                    <div className="m4-success">
                        <div className="m4-success-icon">✓</div>
                        <h2>{result.returnId ? `Return #${result.returnId} recorded` : "Lost note recorded"}</h2>
                        <p className="m4-muted">
                            Rental #{result.rentalId} is now <Badge value={result.rentalStatus} />
                            {" "}· {result.unitsReturned} unit(s) returned{result.unitsLost > 0 && `, ${result.unitsLost} lost`}
                        </p>
                        <div className="m4-chips-row">
                            {num(result.lateCharges) > 0 && <Badge value="LATE">Late {money(result.lateCharges)}</Badge>}
                            {num(result.damageCharges) > 0 && <Badge value="DAMAGE">Damage {money(result.damageCharges)}</Badge>}
                            {num(result.lostItemCharges) > 0 && <Badge value="LOST_ITEM">Lost {money(result.lostItemCharges)}</Badge>}
                        </div>
                        <div className="m4-success-actions">
                            {result.readyForSettlement && (
                                <button className="m4-btn m4-btn-primary m4-btn-lg"
                                        onClick={() => navigate(`/returns/settlements/${result.rentalId}`)}>
                                    Go to settlement →
                                </button>
                            )}
                            {result.returnId && (
                                <button className="m4-btn m4-btn-lg" onClick={() => navigate(`/returns/${result.returnId}`)}>View return</button>
                            )}
                            <button className="m4-btn m4-btn-lg" onClick={startOver}>Process another return</button>
                        </div>
                    </div>
                </Card>
            )}
        </>
    );
}

export default ProcessReturn;
