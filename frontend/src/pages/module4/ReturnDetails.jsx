import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { getReturnDetails } from "../../services/module4/module4Api";
import { Alert, Badge, Card, Empty, Loading, date, dateTime, money } from "../../components/module4/m4ui";

// =========================================================
// MODULE 4 - one return: items, condition, damage assessed.
// =========================================================

function ReturnDetails() {
    const { returnId } = useParams();
    const navigate = useNavigate();
    const [data, setData] = useState(null);
    const [error, setError] = useState("");

    useEffect(() => {
        getReturnDetails(returnId).then(setData).catch((err) => setError(err.message));
    }, [returnId]);

    if (error) return <Alert>{error}</Alert>;
    if (!data) return <Loading />;

    const s = data.summary;
    const canSettle = ["RETURNED"].includes(data.rentalStatus);

    return (
        <>
            <button className="m4-back" onClick={() => navigate("/returns/history")}>← Return history</button>
            <div className="m4-head">
                <div>
                    <h2>Return #{s.returnId} <Badge value={s.returnType} /></h2>
                    <p>
                        Rental #{s.rentalId} · {s.customerName} · returned {dateTime(s.returnDate)}
                        {s.processedByName && <> · by {s.processedByName}</>}
                    </p>
                </div>
                <div style={{ display: "flex", gap: 8 }}>
                    {["ACTIVE", "OVERDUE", "PARTIALLY_RETURNED"].includes(data.rentalStatus) && (
                        <button className="m4-btn" onClick={() => navigate(`/returns/new?rentalId=${s.rentalId}`)}>Return more items</button>
                    )}
                    <button className={`m4-btn ${canSettle ? "m4-btn-primary" : ""}`}
                            onClick={() => navigate(`/returns/settlements/${s.rentalId}`)}>
                        {canSettle ? "Settle rental →" : "Rental billing"}
                    </button>
                </div>
            </div>

            <div className="m4-rental-strip">
                <div className="m4-kv"><span>Rental status</span><Badge value={data.rentalStatus} /></div>
                <div className="m4-kv"><span>Due date</span><strong>{date(data.dueDate)}</strong></div>
                <div className="m4-kv"><span>Units in this return</span><strong>{s.totalUnits}</strong></div>
                <div className="m4-kv"><span>Damaged</span><strong>{s.damagedUnits}</strong></div>
                {s.notes && <div className="m4-kv"><span>Notes</span><strong>{s.notes}</strong></div>}
            </div>

            <Card title="Items returned" flush>
                <div className="m4-table-wrap">
                    <table className="m4-table">
                        <thead>
                        <tr><th>Item</th><th className="m4-right">Qty</th><th>Condition</th><th>Inspection notes</th></tr>
                        </thead>
                        <tbody>
                        {data.items.map((i) => (
                            <tr key={i.returnItemId}>
                                <td>{i.equipmentName}<span className="m4-sub">{i.itemCode}</span></td>
                                <td className="m4-right">{i.quantityReturned}</td>
                                <td><Badge value={i.conditionStatus} /></td>
                                <td>{i.inspectionNotes || <span className="m4-muted">-</span>}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </Card>

            <Card title="Damage assessed"
                  actions={data.damages.length > 0 && <button className="m4-btn m4-btn-sm" onClick={() => navigate("/returns/damages")}>Manage damage</button>}
                  flush>
                {data.damages.length === 0 ? <Empty>No damage on this return.</Empty> : (
                    <div className="m4-table-wrap">
                        <table className="m4-table">
                            <thead>
                            <tr><th>Item</th><th>Damage</th><th>Level</th><th className="m4-right">Units</th><th className="m4-right">Estimate</th><th className="m4-right">Charged</th><th>Status</th></tr>
                            </thead>
                            <tbody>
                            {data.damages.map((d) => (
                                <tr key={d.damageId}>
                                    <td>{d.equipmentName}</td>
                                    <td>{d.damageDescription}</td>
                                    <td><Badge value={d.damageLevel} /></td>
                                    <td className="m4-right">{d.damagedQuantity}</td>
                                    <td className="m4-right">{money(d.estimatedCost)}</td>
                                    <td className="m4-right">{money(d.finalCharge)}</td>
                                    <td><Badge value={d.status} /></td>
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

export default ReturnDetails;
