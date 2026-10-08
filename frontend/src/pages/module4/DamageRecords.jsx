import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { chargeDamage, getDamages, markDamageRepaired, waiveDamage } from "../../services/module4/module4Api";
import { Alert, Badge, Card, Empty, Field, Loading, Modal, dateTime, money } from "../../components/module4/m4ui";

// =========================================================
// MODULE 4 - Damage records.
// Damage is assessed while processing a return; here staff
// follow it up: charge it, waive it, or put repaired units
// back into stock.
// =========================================================

const FILTERS = [["", "All"], ["ASSESSED", "Not charged"], ["CHARGED", "Charged"], ["WAIVED", "Waived"], ["REPAIRED", "Repaired"]];

function DamageRecords() {
    const navigate = useNavigate();
    const [status, setStatus] = useState("");
    const [rows, setRows] = useState(null);
    const [error, setError] = useState("");
    const [notice, setNotice] = useState("");
    const [charging, setCharging] = useState(null);
    const [amount, setAmount] = useState("");
    const [busy, setBusy] = useState(false);

    const load = () => getDamages(status).then(setRows).catch((err) => setError(err.message));

    useEffect(() => {
        setRows(null);
        load();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [status]);

    async function act(fn, message) {
        setError("");
        setBusy(true);
        try {
            await fn();
            setNotice(message);
            await load();
        } catch (err) {
            setError(err.message);
        } finally {
            setBusy(false);
        }
    }

    function openCharge(d) {
        setCharging(d);
        setAmount(d.estimatedCost > 0 ? String(d.estimatedCost) : "");
    }

    async function submitCharge() {
        const d = charging;
        setCharging(null);
        await act(() => chargeDamage(d.damageId, Number(amount)), `Damage #${d.damageId} charged ${money(amount)} to rental #${d.rentalId}.`);
    }

    return (
        <>
            <div className="m4-head">
                <div>
                    <h2>Damage records</h2>
                    <p>Damaged units stay out of stock until they are marked repaired.</p>
                </div>
                <div className="m4-filters">
                    {FILTERS.map(([v, t]) => (
                        <button key={v} className={`m4-chip ${status === v ? "active" : ""}`} onClick={() => setStatus(v)}>{t}</button>
                    ))}
                </div>
            </div>
            <Alert onClose={() => setError("")}>{error}</Alert>
            <Alert type="success" onClose={() => setNotice("")}>{notice}</Alert>

            <Card flush>
                {!rows ? <Loading /> : rows.length === 0 ? <Empty>No damage records here.</Empty> : (
                    <div className="m4-table-wrap">
                        <table className="m4-table">
                            <thead>
                            <tr>
                                <th>Item</th><th>Damage</th><th>Rental</th><th className="m4-right">Units</th>
                                <th className="m4-right">Estimate</th><th className="m4-right">Charge</th><th>Status</th><th></th>
                            </tr>
                            </thead>
                            <tbody>
                            {rows.map((d) => {
                                const closed = d.rentalStatus === "CLOSED";
                                return (
                                    <tr key={d.damageId}>
                                        <td>
                                            <strong>{d.equipmentName}</strong>
                                            <span className="m4-sub">#{d.damageId} · {dateTime(d.assessmentDate)}</span>
                                        </td>
                                        <td>
                                            {d.damageDescription}
                                            <span className="m4-sub"><Badge value={d.damageLevel} /> {d.assessedByName && `by ${d.assessedByName}`}</span>
                                        </td>
                                        <td>
                                            <button className="m4-link" onClick={() => navigate(`/returns/${d.returnId}`)}>Return #{d.returnId}</button>
                                            <span className="m4-sub">rental #{d.rentalId} · {d.customerName}</span>
                                        </td>
                                        <td className="m4-right">{d.damagedQuantity}</td>
                                        <td className="m4-right">{money(d.estimatedCost)}</td>
                                        <td className="m4-right">{money(d.finalCharge)}</td>
                                        <td><Badge value={d.status} /></td>
                                        <td>
                                            <div className="m4-actions">
                                                {d.status === "ASSESSED" && !closed && (
                                                    <button className="m4-btn m4-btn-primary m4-btn-sm" disabled={busy} onClick={() => openCharge(d)}>Charge</button>
                                                )}
                                                {["ASSESSED", "CHARGED"].includes(d.status) && !closed && (
                                                    <button className="m4-btn m4-btn-sm" disabled={busy}
                                                            onClick={() => window.confirm(`Waive the charge for damage #${d.damageId}?`)
                                                                && act(() => waiveDamage(d.damageId), `Damage #${d.damageId} waived; its charge was removed from the bill.`)}>
                                                        Waive
                                                    </button>
                                                )}
                                                {d.status !== "REPAIRED" && (
                                                    <button className="m4-btn m4-btn-sm" disabled={busy}
                                                            onClick={() => act(() => markDamageRepaired(d.damageId),
                                                                `${d.damagedQuantity} × ${d.equipmentName} back in stock.`)}>
                                                        Repaired → stock
                                                    </button>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                );
                            })}
                            </tbody>
                        </table>
                    </div>
                )}
            </Card>

            {charging && (
                <Modal title={`Charge damage #${charging.damageId}`} onClose={() => setCharging(null)}
                       footer={<>
                           <button className="m4-btn" onClick={() => setCharging(null)}>Cancel</button>
                           <button className="m4-btn m4-btn-primary" disabled={!(Number(amount) > 0)} onClick={submitCharge}>Add charge</button>
                       </>}>
                    <p className="m4-muted" style={{ margin: 0 }}>
                        {charging.equipmentName} × {charging.damagedQuantity}: {charging.damageDescription}.
                        The amount is added to rental #{charging.rentalId}'s invoice.
                    </p>
                    <Field label="Charge to renter (Rs.)" hint={`Estimated repair: ${money(charging.estimatedCost)}`}>
                        <input className="m4-input" type="number" min="0" autoFocus value={amount}
                               onChange={(e) => setAmount(e.target.value)} />
                    </Field>
                </Modal>
            )}
        </>
    );
}

export default DamageRecords;
