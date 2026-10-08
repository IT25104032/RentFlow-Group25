import { useEffect, useState } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";

import { getOpenRentals, getReturns } from "../../services/module4/module4Api";
import { Alert, Badge, Card, Empty, Loading, Stat, date, dateTime } from "../../components/module4/m4ui";

// =========================================================
// MODULE 4 - Overview: what needs attention today, rentals
// waiting to come back, and the latest returns.
// =========================================================

function ReturnDashboard() {
    const navigate = useNavigate();
    const { counts } = useOutletContext() || {};
    const [rentals, setRentals] = useState(null);
    const [returns, setReturns] = useState(null);
    const [error, setError] = useState("");

    useEffect(() => {
        Promise.all([getOpenRentals(), getReturns()])
            .then(([open, recent]) => {
                setRentals(open);
                setReturns(recent.slice(0, 8));
            })
            .catch((err) => setError(err.message));
    }, []);

    const c = counts || {};

    return (
        <>
            <Alert onClose={() => setError("")}>{error}</Alert>

            <div className="m4-stats">
                <Stat label="Rentals out" value={c.rentalsOut ?? "-"} hint="waiting to be returned"
                      tone="teal" onClick={() => navigate("/returns/new")} />
                <Stat label="Overdue" value={c.overdue ?? "-"} hint="past the due date"
                      tone={c.overdue ? "red" : undefined} onClick={() => navigate("/returns/new")} />
                <Stat label="Damage to resolve" value={c.damagesToResolve ?? "-"} hint="assessed or awaiting repair"
                      tone={c.damagesToResolve ? "amber" : undefined} onClick={() => navigate("/returns/damages")} />
                <Stat label="Lost, not charged" value={c.lostPending ?? "-"} hint="lost notes without a charge"
                      tone={c.lostPending ? "amber" : undefined} onClick={() => navigate("/returns/lost")} />
                <Stat label="Ready to settle" value={c.readyToSettle ?? "-"} hint="everything back, bill open"
                      tone={c.readyToSettle ? "green" : undefined} onClick={() => navigate("/returns/settlements")} />
            </div>

            <div className="m4-grid-2">
                <Card
                    title="Waiting to be returned"
                    actions={<button className="m4-btn m4-btn-primary m4-btn-sm" onClick={() => navigate("/returns/new")}>Process a return</button>}
                    flush
                >
                    {!rentals ? <Loading /> : rentals.length === 0 ? (
                        <Empty>No equipment is out right now.</Empty>
                    ) : (
                        <div className="m4-table-wrap">
                            <table className="m4-table">
                                <thead>
                                <tr><th>Rental</th><th>Due</th><th className="m4-right">Units out</th><th></th></tr>
                                </thead>
                                <tbody>
                                {rentals.slice(0, 8).map((r) => (
                                    <tr key={r.rentalId} className="m4-row-link"
                                        onClick={() => navigate(`/returns/new?rentalId=${r.rentalId}`)}>
                                        <td>
                                            <strong>#{r.rentalId}</strong> {r.customerName}
                                            <span className="m4-sub">{r.customerPhone}</span>
                                        </td>
                                        <td className="m4-nowrap">
                                            {date(r.dueDate)}
                                            {r.daysOverdue > 0 && <span className="m4-sub"><Badge value="OVERDUE">{r.daysOverdue} days late</Badge></span>}
                                        </td>
                                        <td className="m4-right">{r.unitsOut}</td>
                                        <td className="m4-right"><span className="m4-link">Return →</span></td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </Card>

                <Card
                    title="Latest returns"
                    actions={<button className="m4-btn m4-btn-sm" onClick={() => navigate("/returns/history")}>All returns</button>}
                    flush
                >
                    {!returns ? <Loading /> : returns.length === 0 ? (
                        <Empty>No returns recorded yet.</Empty>
                    ) : (
                        <div className="m4-table-wrap">
                            <table className="m4-table">
                                <thead>
                                <tr><th>Return</th><th>When</th><th className="m4-right">Units</th><th>Type</th></tr>
                                </thead>
                                <tbody>
                                {returns.map((r) => (
                                    <tr key={r.returnId} className="m4-row-link" onClick={() => navigate(`/returns/${r.returnId}`)}>
                                        <td>
                                            <strong>#{r.returnId}</strong> rental #{r.rentalId}
                                            <span className="m4-sub">{r.customerName}</span>
                                        </td>
                                        <td className="m4-nowrap">{dateTime(r.returnDate)}</td>
                                        <td className="m4-right">
                                            {r.totalUnits}
                                            {r.damagedUnits > 0 && <span className="m4-sub">{r.damagedUnits} damaged</span>}
                                        </td>
                                        <td><Badge value={r.returnType} /></td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </Card>
            </div>
        </>
    );
}

export default ReturnDashboard;
