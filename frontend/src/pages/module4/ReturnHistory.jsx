import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { getReturns } from "../../services/module4/module4Api";
import { Alert, Badge, Card, Empty, Loading, dateTime } from "../../components/module4/m4ui";

// =========================================================
// MODULE 4 - Return history: every return the company processed.
// =========================================================

function ReturnHistory() {
    const navigate = useNavigate();
    const [search, setSearch] = useState("");
    const [type, setType] = useState("");
    const [rows, setRows] = useState(null);
    const [error, setError] = useState("");

    useEffect(() => {
        const t = setTimeout(() => {
            getReturns(search.trim())
                .then(setRows)
                .catch((err) => setError(err.message));
        }, 250);
        return () => clearTimeout(t);
    }, [search]);

    const visible = (rows || []).filter((r) => !type || r.returnType === type);

    return (
        <>
            <div className="m4-head">
                <div>
                    <h2>Return history</h2>
                    <p>Every check-in, newest first. Open one to see the items, their condition and any damage.</p>
                </div>
                <button className="m4-btn m4-btn-primary" onClick={() => navigate("/returns/new")}>+ Process return</button>
            </div>
            <Alert onClose={() => setError("")}>{error}</Alert>

            <Card flush>
                <div className="m4-card-body" style={{ display: "flex", gap: 12, flexWrap: "wrap", justifyContent: "space-between" }}>
                    <input className="m4-input" style={{ maxWidth: 380 }} placeholder="Search rental number, renter or phone"
                           value={search} onChange={(e) => setSearch(e.target.value)} />
                    <div className="m4-filters">
                        {[["", "All"], ["FULL", "Full"], ["PARTIAL", "Partial"]].map(([v, t]) => (
                            <button key={v} className={`m4-chip ${type === v ? "active" : ""}`} onClick={() => setType(v)}>{t}</button>
                        ))}
                    </div>
                </div>

                {!rows ? <Loading /> : visible.length === 0 ? <Empty>No returns found.</Empty> : (
                    <div className="m4-table-wrap">
                        <table className="m4-table">
                            <thead>
                            <tr>
                                <th>Return</th><th>Rental</th><th>Renter</th><th>Returned</th>
                                <th className="m4-right">Units</th><th>Type</th><th>Processed by</th>
                            </tr>
                            </thead>
                            <tbody>
                            {visible.map((r) => (
                                <tr key={r.returnId} className="m4-row-link" onClick={() => navigate(`/returns/${r.returnId}`)}>
                                    <td><strong>#{r.returnId}</strong></td>
                                    <td>#{r.rentalId}</td>
                                    <td>{r.customerName}</td>
                                    <td className="m4-nowrap">{dateTime(r.returnDate)}</td>
                                    <td className="m4-right">
                                        {r.totalUnits}
                                        {r.damagedUnits > 0 && <span className="m4-sub" style={{ color: "var(--m4-red)" }}>{r.damagedUnits} damaged</span>}
                                    </td>
                                    <td><Badge value={r.returnType} /></td>
                                    <td>{r.processedByName || "-"}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </Card>
        </>
    );
}

export default ReturnHistory;
