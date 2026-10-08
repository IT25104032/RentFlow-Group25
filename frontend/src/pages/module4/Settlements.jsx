import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { getSettlements } from "../../services/module4/module4Api";
import { Alert, Badge, Card, Empty, Loading, date, dateTime, money } from "../../components/module4/m4ui";

// =========================================================
// MODULE 4 - Settlements: rentals with equipment back, and
// whether their bill is settled.
// =========================================================

const FILTERS = [["ready", "Ready to settle"], ["PENDING", "Balance owed"], ["SETTLED", "Settled"], ["out", "Still out"], ["", "All"]];

function stateOf(row) {
    if (row.settlementStatus === "SETTLED") return "SETTLED";
    if (row.unitsOut > 0) return "out";
    if (row.settlementStatus === "PENDING") return "PENDING";
    return "ready";
}

function Settlements() {
    const navigate = useNavigate();
    const [search, setSearch] = useState("");
    const [filter, setFilter] = useState("ready");
    const [rows, setRows] = useState(null);
    const [error, setError] = useState("");

    useEffect(() => {
        const t = setTimeout(() => {
            getSettlements(search.trim()).then(setRows).catch((err) => setError(err.message));
        }, 250);
        return () => clearTimeout(t);
    }, [search]);

    const count = (f) => (rows || []).filter((r) => !f || stateOf(r) === f).length;
    const visible = (rows || []).filter((r) => !filter || stateOf(r) === filter);

    return (
        <>
            <div className="m4-head">
                <div>
                    <h2>Settlements</h2>
                    <p>Total the charges, use the security deposit, collect or refund the difference and close the rental.</p>
                </div>
            </div>
            <Alert onClose={() => setError("")}>{error}</Alert>

            <Card flush>
                <div className="m4-card-body" style={{ display: "flex", gap: 12, flexWrap: "wrap", justifyContent: "space-between" }}>
                    <input className="m4-input" style={{ maxWidth: 340 }} placeholder="Search rental number, renter or phone"
                           value={search} onChange={(e) => setSearch(e.target.value)} />
                    <div className="m4-filters">
                        {FILTERS.map(([v, t]) => (
                            <button key={v} className={`m4-chip ${filter === v ? "active" : ""}`} onClick={() => setFilter(v)}>
                                {t} {rows && <span>({count(v)})</span>}
                            </button>
                        ))}
                    </div>
                </div>

                {!rows ? <Loading /> : visible.length === 0 ? <Empty>Nothing here.</Empty> : (
                    <div className="m4-table-wrap">
                        <table className="m4-table">
                            <thead>
                            <tr>
                                <th>Rental</th><th>Renter</th><th>Due</th><th>Rental status</th>
                                <th className="m4-right">Total charges</th><th className="m4-right">Balance</th><th>Settlement</th><th></th>
                            </tr>
                            </thead>
                            <tbody>
                            {visible.map((r) => {
                                const s = stateOf(r);
                                return (
                                    <tr key={r.rentalId} className="m4-row-link" onClick={() => navigate(`/returns/settlements/${r.rentalId}`)}>
                                        <td><strong>#{r.rentalId}</strong></td>
                                        <td>{r.customerName}</td>
                                        <td className="m4-nowrap">{date(r.dueDate)}</td>
                                        <td><Badge value={r.rentalStatus} />{r.unitsOut > 0 && <span className="m4-sub">{r.unitsOut} unit(s) out</span>}</td>
                                        <td className="m4-right">{money(r.totalCharges)}</td>
                                        <td className="m4-right">{Number(r.balanceDue) > 0 ? <strong>{money(r.balanceDue)}</strong> : money(r.balanceDue)}</td>
                                        <td>
                                            {s === "SETTLED" ? <><Badge value="SETTLED" /><span className="m4-sub">{dateTime(r.settledAt)}</span></>
                                                : s === "PENDING" ? <Badge value="PENDING">Balance owed</Badge>
                                                    : s === "ready" ? <Badge value="RETURNED">Ready</Badge>
                                                        : <Badge value="ACTIVE">Waiting for items</Badge>}
                                        </td>
                                        <td className="m4-right">
                                            <span className="m4-link">{s === "SETTLED" ? "View" : s === "out" ? "Open" : "Settle →"}</span>
                                        </td>
                                    </tr>
                                );
                            })}
                            </tbody>
                        </table>
                    </div>
                )}
            </Card>
        </>
    );
}

export default Settlements;
