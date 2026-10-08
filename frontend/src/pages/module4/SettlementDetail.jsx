import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { getSettlement, paySettlement, settleRental } from "../../services/module4/module4Api";
import {
    Alert, Badge, Card, Empty, Field, Loading, Modal, RentalFlow, date, dateTime, money
} from "../../components/module4/m4ui";

// =========================================================
// MODULE 4 - Final settlement of one rental.
// Shows every charge, what was paid, the deposit, and what the
// renter still owes or gets back. "Settle" uses the deposit; if
// money is still owed, collect it here and the rental closes.
// =========================================================

function Row({ text, value, minus, className = "" }) {
    return (
        <div className={`m4-breakdown-row ${className}`}>
            <span>{text}</span>
            <span>{minus && Number(value) > 0 ? "− " : ""}{money(value)}</span>
        </div>
    );
}

function SettlementDetail() {
    const { rentalId } = useParams();
    const navigate = useNavigate();
    const [s, setS] = useState(null);
    const [error, setError] = useState("");
    const [notice, setNotice] = useState("");
    const [busy, setBusy] = useState(false);
    const [confirming, setConfirming] = useState(false);
    const [paying, setPaying] = useState(false);
    const [payment, setPayment] = useState({ amount: "", paymentMethod: "CASH", referenceNo: "" });

    useEffect(() => {
        setS(null);
        getSettlement(rentalId).then(setS).catch((err) => setError(err.message));
    }, [rentalId]);

    async function settle() {
        setConfirming(false);
        setBusy(true);
        setError("");
        try {
            const next = await settleRental(rentalId);
            setS(next);
            setNotice(next.settlementStatus === "SETTLED"
                ? `Rental #${rentalId} is settled and closed.`
                : `Deposit applied. ${next.customerName} still owes ${money(next.outstanding)}; collect it below to close the rental.`);
        } catch (err) {
            setError(err.message);
        } finally {
            setBusy(false);
        }
    }

    async function pay() {
        setBusy(true);
        setError("");
        try {
            const next = await paySettlement(rentalId, { ...payment, amount: Number(payment.amount) });
            setPaying(false);
            setS(next);
            setNotice(next.settlementStatus === "SETTLED"
                ? `Payment recorded. Rental #${rentalId} is settled and closed.`
                : `Payment recorded. ${money(next.outstanding)} still owed.`);
        } catch (err) {
            setError(err.message);
        } finally {
            setBusy(false);
        }
    }

    if (!s) return error ? <Alert>{error}</Alert> : <Loading />;

    const settled = s.settlementStatus === "SETTLED";
    const pending = s.settlementStatus === "PENDING";

    return (
        <>
            <button className="m4-back" onClick={() => navigate("/returns/settlements")}>← Settlements</button>
            <div className="m4-head">
                <div>
                    <h2>Rental #{s.rentalId} <Badge value={s.rentalStatus} /></h2>
                    <p>{s.customerName} · {s.customerPhone || "-"} · {date(s.startDate)} → {date(s.dueDate)}</p>
                </div>
                <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                    {s.unitsOut > 0 && (
                        <button className="m4-btn m4-btn-primary" onClick={() => navigate(`/returns/new?rentalId=${s.rentalId}`)}>
                            Return remaining {s.unitsOut} unit(s)
                        </button>
                    )}
                    {!settled && s.allItemsResolved && s.settlementStatus !== "PENDING" && (
                        <button className="m4-btn m4-btn-primary m4-btn-lg" disabled={busy} onClick={() => setConfirming(true)}>Settle</button>
                    )}
                    {!settled && s.settlementStatus === "PENDING" && (
                        <button className="m4-btn m4-btn-primary m4-btn-lg" disabled={busy}
                                onClick={() => { setPayment({ amount: String(s.outstanding), paymentMethod: "CASH", referenceNo: "" }); setPaying(true); }}>
                            Collect {money(s.outstanding)}
                        </button>
                    )}
                </div>
            </div>

            <RentalFlow status={s.rentalStatus} settlementStatus={s.settlementStatus} />

            <Alert onClose={() => setError("")}>{error}</Alert>
            <Alert type="success" onClose={() => setNotice("")}>{notice}</Alert>
            {s.unitsOut > 0 && (
                <Alert type="warn">{s.unitsOut} unit(s) are still with the renter. Return them or record them as lost before settling.</Alert>
            )}

            <div className="m4-grid-2">
                <Card title={settled ? "Final settlement" : pending ? "Settlement · balance owed" : "Settlement preview"}>
                    <div className="m4-breakdown">
                        <Row text="Rental + extension + other" value={s.rentalCharges} />
                        <Row text="Late charges" value={s.lateCharges} />
                        <Row text="Damage charges" value={s.damageCharges} />
                        <Row text="Lost-item charges" value={s.lostItemCharges} />
                        <Row text="Total charges" value={s.totalCharges} className="total" />
                        {settled || pending ? (
                            <>
                                <Row text={`Paid (incl. ${money(s.settledDepositUsed)} from the deposit)`} value={s.alreadyPaid} minus />
                                <Row text="Deposit refunded to renter" value={s.settledDepositRefunded} className="total" />
                                <Row text={settled ? "Balance" : "Renter still owes"} value={s.outstanding} className="total big" />
                            </>
                        ) : (
                            <>
                                <Row text="Already paid" value={s.alreadyPaid} minus />
                                <Row text="Outstanding" value={s.outstanding} className="total" />
                                <Row text="Deposit held" value={s.depositHeld} />
                                <Row text="Taken from deposit" value={s.depositToUse} minus />
                                <Row text="Deposit to refund to renter" value={s.depositToRefund} className="total" />
                                <Row text="Renter still owes" value={s.balanceAfterDeposit} className="total big" />
                            </>
                        )}
                    </div>
                    <p className="m4-muted m4-small" style={{ marginBottom: 0 }}>
                        {settled
                            ? `Settled ${dateTime(s.settledAt)}. The rental is closed.`
                            : pending
                                ? "The deposit has been used. Collect the balance to close the rental."
                                : "Settling puts every charge on the invoice, pays it from the deposit (recorded as a DEPOSIT-ADJUSTMENT payment) and refunds the rest. If money is still owed, collect it here and the rental closes."}
                    </p>
                </Card>

                <Card title="Security deposit">
                    <div className="m4-rental-strip" style={{ margin: 0, border: 0, padding: 0 }}>
                        <div className="m4-kv"><span>Status</span>{s.depositStatus ? <Badge value={s.depositStatus} /> : <Badge value="CANCELLED">None recorded</Badge>}</div>
                        <div className="m4-kv"><span>Calculated</span><strong>{money(s.depositCalculated)}</strong></div>
                        <div className="m4-kv"><span>Held now</span><strong>{money(s.depositHeld)}</strong></div>
                    </div>
                    {!s.depositStatus && (
                        <p className="m4-muted m4-small">No deposit was recorded for this rental in billing (Module 3), so nothing is deducted.</p>
                    )}
                    <div style={{ marginTop: 14 }}>
                        <strong>Invoice</strong>{" "}
                        {s.invoiceId ? <>INV-{s.invoiceId} · <button className="m4-link" onClick={() => navigate("/payments")}>open in Invoices &amp; Payments</button></>
                            : <span className="m4-muted">not created yet (created when settling)</span>}
                    </div>
                </Card>
            </div>

            <Card title="Charges" flush>
                {s.charges.length === 0 ? <Empty>No charges on this rental.</Empty> : (
                    <div className="m4-table-wrap">
                        <table className="m4-table">
                            <thead>
                            <tr><th>Date</th><th>Type</th><th>Description</th><th className="m4-right">Amount</th><th>Invoice</th></tr>
                            </thead>
                            <tbody>
                            {s.charges.map((c) => (
                                <tr key={c.chargeId}>
                                    <td className="m4-nowrap">{dateTime(c.chargeDate)}</td>
                                    <td><Badge value={c.chargeType} /></td>
                                    <td>{c.description || "-"}</td>
                                    <td className="m4-right">{money(c.amount)}</td>
                                    <td>{c.invoiceId ? `INV-${c.invoiceId}` : <span className="m4-muted">not invoiced</span>}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </Card>

            <Card title="Returns" flush>
                {s.returns.length === 0 ? <Empty>No returns yet.</Empty> : (
                    <div className="m4-table-wrap">
                        <table className="m4-table">
                            <thead>
                            <tr><th>Return</th><th>When</th><th className="m4-right">Units</th><th>Type</th><th>By</th></tr>
                            </thead>
                            <tbody>
                            {s.returns.map((r) => (
                                <tr key={r.returnId} className="m4-row-link" onClick={() => navigate(`/returns/${r.returnId}`)}>
                                    <td><strong>#{r.returnId}</strong></td>
                                    <td>{dateTime(r.returnDate)}</td>
                                    <td className="m4-right">{r.totalUnits}{r.damagedUnits > 0 && <span className="m4-sub">{r.damagedUnits} damaged</span>}</td>
                                    <td><Badge value={r.returnType} /></td>
                                    <td>{r.processedByName || "-"}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </Card>

            {confirming && (
                <Modal title={`Final settlement · rental #${s.rentalId}`} onClose={() => setConfirming(false)}
                       footer={<>
                           <button className="m4-btn" onClick={() => setConfirming(false)}>Cancel</button>
                           <button className="m4-btn m4-btn-primary" disabled={busy} onClick={settle}>Settle</button>
                       </>}>
                    <div className="m4-breakdown">
                        <Row text="Total charges" value={s.totalCharges} />
                        <Row text="Already paid" value={s.alreadyPaid} minus />
                        <Row text="Outstanding" value={s.outstanding} className="total" />
                        <Row text="Taken from deposit" value={s.depositToUse} minus />
                        <Row text="Deposit to refund" value={s.depositToRefund} className="total" />
                        <Row text="Renter still owes" value={s.balanceAfterDeposit} className="total big" />
                    </div>
                    <p className="m4-muted m4-small" style={{ margin: 0 }}>
                        {Number(s.balanceAfterDeposit) > 0
                            ? "The renter will still owe the balance; the settlement stays pending until you collect it."
                            : "Nothing will be owed, so the rental will be closed."}
                    </p>
                </Modal>
            )}

            {paying && (
                <Modal title={`Collect payment · rental #${s.rentalId}`} onClose={() => setPaying(false)}
                       footer={<>
                           <button className="m4-btn" onClick={() => setPaying(false)}>Cancel</button>
                           <button className="m4-btn m4-btn-primary" disabled={busy || !(Number(payment.amount) > 0)} onClick={pay}>Record payment</button>
                       </>}>
                    <div className="m4-form-row">
                        <Field label="Amount (Rs.)" hint={`Owed: ${money(s.outstanding)}`}>
                            <input className="m4-input" type="number" min="0" max={s.outstanding} autoFocus value={payment.amount}
                                   onChange={(e) => setPayment({ ...payment, amount: e.target.value })} />
                        </Field>
                        <Field label="Method">
                            <select className="m4-select" value={payment.paymentMethod}
                                    onChange={(e) => setPayment({ ...payment, paymentMethod: e.target.value })}>
                                <option value="CASH">Cash</option>
                                <option value="CARD">Card</option>
                                <option value="BANK_TRANSFER">Bank transfer</option>
                            </select>
                        </Field>
                    </div>
                    <Field label="Reference" hint="optional, e.g. card slip or transfer number">
                        <input className="m4-input" value={payment.referenceNo}
                               onChange={(e) => setPayment({ ...payment, referenceNo: e.target.value })} />
                    </Field>
                </Modal>
            )}
        </>
    );
}

export default SettlementDetail;
