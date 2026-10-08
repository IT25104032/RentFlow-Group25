import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
    chargeLostItem, getLostItems, getOpenRentals, getRentalForReturn, recordLostItem, recoverLostItem
} from "../../services/module4/module4Api";
import { Alert, Badge, Card, Empty, Field, Loading, Modal, dateTime, money } from "../../components/module4/m4ui";

// =========================================================
// MODULE 4 - Lost items (lost notes).
// A lost note can be written during Process Return or here.
// Lost units leave the company's stock; if they turn up again,
// "Recovered" puts them back and removes the charge.
// =========================================================

const FILTERS = [["", "All"], ["PENDING", "Not charged"], ["CHARGED", "Charged"], ["RECOVERED", "Recovered"], ["SETTLED", "Settled"]];
const EMPTY_FORM = { rentalId: "", rentalItemId: "", quantityLost: 1, lossType: "NON_RETURNED", replacementCostPerUnit: "", reason: "", notes: "" };

function LostItems() {
    const navigate = useNavigate();
    const [status, setStatus] = useState("");
    const [rows, setRows] = useState(null);
    const [error, setError] = useState("");
    const [notice, setNotice] = useState("");
    const [busy, setBusy] = useState(false);

    // new lost note
    const [formOpen, setFormOpen] = useState(false);
    const [form, setForm] = useState(EMPTY_FORM);
    const [openRentals, setOpenRentals] = useState([]);
    const [items, setItems] = useState([]);
    const [formError, setFormError] = useState("");

    // charge a pending note
    const [charging, setCharging] = useState(null);
    const [cost, setCost] = useState("");

    const load = () => getLostItems(status).then(setRows).catch((err) => setError(err.message));

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

    async function openForm() {
        setForm(EMPTY_FORM);
        setItems([]);
        setFormError("");
        setFormOpen(true);
        try {
            setOpenRentals(await getOpenRentals());
        } catch (err) {
            setFormError(err.message);
        }
    }

    async function chooseRental(rentalId) {
        setForm((f) => ({ ...f, rentalId, rentalItemId: "" }));
        setItems([]);
        if (!rentalId) return;
        try {
            const r = await getRentalForReturn(rentalId);
            const out = r.items.filter((i) => i.remainingQuantity > 0);
            setItems(out);
            if (out.length === 1) setForm((f) => ({ ...f, rentalItemId: String(out[0].rentalItemId) }));
        } catch (err) {
            setFormError(err.message);
        }
    }

    const chosenItem = items.find((i) => String(i.rentalItemId) === String(form.rentalItemId));

    async function saveLost() {
        setFormError("");
        try {
            const saved = await recordLostItem({
                ...form,
                rentalId: Number(form.rentalId),
                rentalItemId: Number(form.rentalItemId),
                quantityLost: Number(form.quantityLost),
                replacementCostPerUnit: Number(form.replacementCostPerUnit || 0)
            });
            setFormOpen(false);
            setNotice(`Lost note #${saved.lostItemId} recorded for rental #${saved.rentalId}.`);
            load();
        } catch (err) {
            setFormError(err.message);
        }
    }

    async function submitCharge() {
        const l = charging;
        setCharging(null);
        await act(() => chargeLostItem(l.lostItemId, Number(cost)),
            `Lost note #${l.lostItemId} charged ${money(Number(cost) * l.quantityLost)}.`);
    }

    return (
        <>
            <div className="m4-head">
                <div>
                    <h2>Lost items</h2>
                    <p>Units that did not come back. They are written out of stock and charged at replacement cost.</p>
                </div>
                <button className="m4-btn m4-btn-primary" onClick={openForm}>+ Record lost item</button>
            </div>
            <div className="m4-filters" style={{ marginBottom: 14 }}>
                {FILTERS.map(([v, t]) => (
                    <button key={v} className={`m4-chip ${status === v ? "active" : ""}`} onClick={() => setStatus(v)}>{t}</button>
                ))}
            </div>
            <Alert onClose={() => setError("")}>{error}</Alert>
            <Alert type="success" onClose={() => setNotice("")}>{notice}</Alert>

            <Card flush>
                {!rows ? <Loading /> : rows.length === 0 ? <Empty>No lost notes here.</Empty> : (
                    <div className="m4-table-wrap">
                        <table className="m4-table">
                            <thead>
                            <tr>
                                <th>Item</th><th>Rental</th><th className="m4-right">Qty</th><th>Type</th>
                                <th>Reason</th><th className="m4-right">Charge</th><th>Status</th><th></th>
                            </tr>
                            </thead>
                            <tbody>
                            {rows.map((l) => {
                                const open = ["PENDING", "CHARGED"].includes(l.status) && l.rentalStatus !== "CLOSED";
                                return (
                                    <tr key={l.lostItemId}>
                                        <td>
                                            <strong>{l.equipmentName}</strong>
                                            <span className="m4-sub">#{l.lostItemId} · {dateTime(l.reportedDate)}</span>
                                        </td>
                                        <td>
                                            <button className="m4-link" onClick={() => navigate(`/returns/settlements/${l.rentalId}`)}>Rental #{l.rentalId}</button>
                                            <span className="m4-sub">{l.customerName}</span>
                                        </td>
                                        <td className="m4-right">{l.quantityLost}</td>
                                        <td><Badge value={l.lossType} /></td>
                                        <td>{l.reason || <span className="m4-muted">-</span>}</td>
                                        <td className="m4-right">
                                            {money(l.chargeAmount)}
                                            {Number(l.replacementCostPerUnit) > 0 && <span className="m4-sub">{money(l.replacementCostPerUnit)} each</span>}
                                        </td>
                                        <td><Badge value={l.status} /></td>
                                        <td>
                                            <div className="m4-actions">
                                                {l.status === "PENDING" && open && (
                                                    <button className="m4-btn m4-btn-primary m4-btn-sm" disabled={busy}
                                                            onClick={() => { setCharging(l); setCost(""); }}>Charge</button>
                                                )}
                                                {open && (
                                                    <button className="m4-btn m4-btn-sm" disabled={busy}
                                                            onClick={() => window.confirm(`Mark ${l.quantityLost} × ${l.equipmentName} as found? Stock is restored and the charge removed.`)
                                                                && act(() => recoverLostItem(l.lostItemId), `${l.equipmentName} recovered and back in stock.`)}>
                                                        Recovered
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

            {formOpen && (
                <Modal title="Record lost item" onClose={() => setFormOpen(false)}
                       footer={<>
                           <button className="m4-btn" onClick={() => setFormOpen(false)}>Cancel</button>
                           <button className="m4-btn m4-btn-primary" disabled={!form.rentalItemId} onClick={saveLost}>Save lost note</button>
                       </>}>
                    <Alert onClose={() => setFormError("")}>{formError}</Alert>
                    <Field label="Rental">
                        <select className="m4-select" value={form.rentalId} onChange={(e) => chooseRental(e.target.value)}>
                            <option value="">Choose a rental with equipment out</option>
                            {openRentals.map((r) => (
                                <option key={r.rentalId} value={r.rentalId}>#{r.rentalId} · {r.customerName} · {r.unitsOut} out</option>
                            ))}
                        </select>
                    </Field>
                    {form.rentalId && (
                        <Field label="Item" hint={chosenItem && `${chosenItem.remainingQuantity} of ${chosenItem.issuedQuantity} still out`}>
                            <select className="m4-select" value={form.rentalItemId}
                                    onChange={(e) => setForm({ ...form, rentalItemId: e.target.value })}>
                                <option value="">Choose the item</option>
                                {items.map((i) => (
                                    <option key={i.rentalItemId} value={i.rentalItemId}>{i.equipmentName} ({i.itemCode})</option>
                                ))}
                            </select>
                        </Field>
                    )}
                    <div className="m4-form-row">
                        <Field label="Units lost">
                            <input className="m4-input" type="number" min="1" max={chosenItem?.remainingQuantity}
                                   value={form.quantityLost} onChange={(e) => setForm({ ...form, quantityLost: e.target.value })} />
                        </Field>
                        <Field label="Type">
                            <select className="m4-select" value={form.lossType} onChange={(e) => setForm({ ...form, lossType: e.target.value })}>
                                <option value="NON_RETURNED">Not returned</option>
                                <option value="LOST">Lost</option>
                                <option value="STOLEN">Stolen</option>
                            </select>
                        </Field>
                        <Field label="Replacement cost / unit (Rs.)" hint="0 = charge later">
                            <input className="m4-input" type="number" min="0" value={form.replacementCostPerUnit}
                                   onChange={(e) => setForm({ ...form, replacementCostPerUnit: e.target.value })} />
                        </Field>
                    </div>
                    <Field label="Reason">
                        <input className="m4-input" value={form.reason} onChange={(e) => setForm({ ...form, reason: e.target.value })} />
                    </Field>
                    {Number(form.replacementCostPerUnit) > 0 && (
                        <Alert type="info">Charge: {money(Number(form.replacementCostPerUnit) * Number(form.quantityLost || 0))} on the rental's invoice.</Alert>
                    )}
                </Modal>
            )}

            {charging && (
                <Modal title={`Charge lost note #${charging.lostItemId}`} onClose={() => setCharging(null)}
                       footer={<>
                           <button className="m4-btn" onClick={() => setCharging(null)}>Cancel</button>
                           <button className="m4-btn m4-btn-primary" disabled={!(Number(cost) > 0)} onClick={submitCharge}>Add charge</button>
                       </>}>
                    <Field label="Replacement cost per unit (Rs.)"
                           hint={`${charging.quantityLost} × ${charging.equipmentName} = ${money(Number(cost || 0) * charging.quantityLost)}`}>
                        <input className="m4-input" type="number" min="0" autoFocus value={cost} onChange={(e) => setCost(e.target.value)} />
                    </Field>
                </Modal>
            )}
        </>
    );
}

export default LostItems;
