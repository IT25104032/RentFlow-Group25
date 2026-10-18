import { useEffect, useState } from "react";
import { getAllCustomers } from "../../services/module2/customerService";
import "./RentalReports.css";

const API = "http://localhost:8081/api/rentals";
const dateText = (value) =>
    value ? String(value).slice(0, 10) : "—";
const getUser = () => {
    try {
        return JSON.parse(sessionStorage.getItem("rentflowUser") || "null");
    } catch {
        return null;
    }
};

export default function RentalReports() {
    const companyId = getUser()?.companyId;
    const [reportType, setReportType] = useState("current");
    const [rentals, setRentals] = useState([]);
    const [history, setHistory] = useState([]);
    const [selectedCustomerId, setSelectedCustomerId] = useState("");
    const [selectedRentalId, setSelectedRentalId] = useState("");
    const [settlement, setSettlement] = useState(null);
    const [settlementMessage, setSettlementMessage] = useState("");
    const [renterSearch, setRenterSearch] = useState("");
    const [customers, setCustomers] = useState([]);
    const [equipmentNames, setEquipmentNames] = useState({});
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [generatedAt, setGeneratedAt] = useState(null);

    async function generateReport(type = reportType) {
        if (!companyId) {
            setError("Login session not found. Please log in again.");
            return;
        }
        if ((type === "history" || type === "individual") && !selectedCustomerId) {
            setError("Select a renter to generate their full history.");
            setGeneratedAt(null);
            return;
        }
        setLoading(true);
        setError("");
        setGeneratedAt(null);
        setRentals([]);
        setHistory([]);
        try {
            if (type === "history" || type === "individual") {
                const [historyResponse, customerData] = await Promise.all([
                    fetch(`${API}/customer/${encodeURIComponent(selectedCustomerId)}?companyId=${encodeURIComponent(companyId)}`, { credentials: "include" }),
                    getAllCustomers(companyId)
                ]);
                if (!historyResponse.ok) {
                    throw new Error(`Could not retrieve rental history (HTTP ${historyResponse.status}).`);
                }
                if (!Array.isArray(customerData)) {
                    throw new Error("Unexpected renter data from server.");
                }
                if (!customerData.some(c => String(c.customerId) === String(selectedCustomerId))) {
                    throw new Error("The selected renter does not belong to this company.");
                }
                const records = await historyResponse.json();
                if (!Array.isArray(records)) {
                    throw new Error("Unexpected history data from server.");
                }
                const sorted = [...records].sort((a, b) => Number(a.rental?.rentalId) - Number(b.rental?.rentalId));
                setHistory(sorted);
                if (type === "individual") {
                    setSelectedRentalId(previous =>
                        sorted.some(r => String(r.rental?.rentalId) === previous)
                            ? previous
                            : String(sorted[0]?.rental?.rentalId ?? "")
                    );
                }
                setCustomers(customerData);
                setGeneratedAt(new Date());
                return;
            }
            const [rentalResponse, customerData] = await Promise.all([
                fetch(`${API}/${type === "overdue" ? "overdue" : "active"}?companyId=${encodeURIComponent(companyId)}`, {
                    credentials: "include"
                }),
                getAllCustomers(companyId)
            ]);
            if (!rentalResponse.ok) {
                throw new Error(`Could not retrieve ${type} rentals (HTTP ${rentalResponse.status}).`);
            }
            const rentalData = await rentalResponse.json();
            if (!Array.isArray(rentalData) || !Array.isArray(customerData)) {
                throw new Error("Unexpected report data from server.");
            }
            setRentals([...rentalData].sort((a, b) => Number(a.rentalId) - Number(b.rentalId)));
            setCustomers(customerData);
            setGeneratedAt(new Date());
        } catch (e) {
            setError(e.message || "Report could not be generated.");
        } finally {
            setLoading(false);
        }
    }


    // Load the company equipment catalog, including inactive equipment
    // that may still appear in older rental history records.
    useEffect(() => {
        if (!companyId) return;
        const controller = new AbortController();

        async function loadEquipmentNames() {
            try {
                const response = await fetch("http://localhost:8081/api/module1/equipment", {
                    credentials: "include",
                    signal: controller.signal
                });
                if (!response.ok) {
                    throw new Error(`Equipment lookup failed (${response.status}).`);
                }
                const equipment = await response.json();
                if (!Array.isArray(equipment)) {
                    throw new Error("Invalid equipment list.");
                }
                setEquipmentNames(Object.fromEntries(
                    equipment.map(item => [String(item.equipmentId), item.itemName])
                ));
            } catch (error) {
                if (error.name !== "AbortError") {
                    console.error("Could not load equipment names:", error);
                }
            }
        }

        loadEquipmentNames();
        return () => controller.abort();
    }, [companyId]);


    // Generate a snapshot once on opening. 'Refresh Report' fetches latest data.
    useEffect(() => {
        if ((reportType !== "history" && reportType !== "individual") || selectedCustomerId) {
            generateReport(reportType);
        } else {
            setGeneratedAt(null);
            setHistory([]);
            setError("");
        }
    }, [companyId, reportType, selectedCustomerId]);

    useEffect(() => {
        if ((reportType !== "history" && reportType !== "individual") || !companyId || customers.length) return;
        let active = true;
        getAllCustomers(companyId).then(data => { if (active && Array.isArray(data)) setCustomers(data); })
            .catch(e => { if (active) setError(e.message || "Could not load renters."); });
        return () => { active = false; };
    }, [reportType, companyId, customers.length]);

    useEffect(() => {
        setSettlement(null);
        setSettlementMessage("");
        if (reportType !== "individual" || !selectedRentalId || !history.some(r => String(r.rental?.rentalId) === String(selectedRentalId))) return;
        let active = true;
        fetch(`http://localhost:8081/api/settlements/${encodeURIComponent(selectedRentalId)}`, { credentials: "include" })
            .then(async response => {
                if (!response.ok) throw new Error(`Settlement details unavailable (HTTP ${response.status}).`);
                return response.json();
            })
            .then(data => { if (active) setSettlement(data); })
            .catch(() => { if (active) setSettlementMessage("Settlement details not available for this rental. No payment total is assumed."); });
        return () => { active = false; };
    }, [reportType, selectedRentalId, history]);

    const customerMap = new Map(customers.map(c => [Number(c.customerId), c]));
    const printable = Boolean(generatedAt) && !loading && !error && (reportType !== "individual" || Boolean(history.find(r => String(r.rental?.rentalId) === String(selectedRentalId))));
    const overdue = reportType === "overdue";
    const isHistory = reportType === "history";
    const isIndividual = reportType === "individual";
    const isRenterReport = isHistory || isIndividual;
    const individualRecord = history.find(record => String(record.rental?.rentalId) === String(selectedRentalId));
    const currency = value => value == null || !Number.isFinite(Number(value)) ? "—" : `Rs. ${Number(value).toLocaleString()}`;
    const displayNotes = value => String(value || "").replace(/\s*\[FINAL_DEPOSIT=-?\d+(?:\.\d+)?\]/g, "").trim() || "No notes recorded.";
    const selectedCustomer = customers.find(c => String(c.customerId) === String(selectedCustomerId));
    const filteredCustomers = customers.filter(c => {
        const q = renterSearch.trim().toLowerCase();
        if (!q) return true;
        return [c.customerName, c.phone, c.email, String(c.customerId)]
            .some(value => String(value ?? "").toLowerCase().includes(q));
    }).sort((a, b) => (a.customerName || "").localeCompare(b.customerName || ""));
    const reportTitle = isIndividual ? "INDIVIDUAL RENTAL REPORT" : isHistory ? "RENTER RENTAL HISTORY REPORT" : (overdue ? "OVERDUE RENTALS REPORT" : "CURRENT RENTALS REPORT");
    function daysOverdue(dueDate) {
        if (!dueDate) return "—";
        const parsed = String(dueDate).slice(0, 10).split("-").map(Number);
        if (parsed.length !== 3 || parsed.some(n => !Number.isFinite(n))) return "—";
        const due = Date.UTC(parsed[0], parsed[1] - 1, parsed[2]);
        const now = generatedAt || new Date();
        const today = Date.UTC(now.getFullYear(), now.getMonth(), now.getDate());
        return Math.max(0, Math.floor((today - due) / 86400000));
    }


    return (
        <main className="rrep-page">
            <div className="rrep-toolbar">
                <div>
                    <h1>Rental Reports</h1>
                    <p>Review the report before printing or saving it as a PDF.</p>
                </div>
                <div className="rrep-actions">
                    <button type="button" className="rrep-secondary" onClick={() => generateReport(reportType)} disabled={loading}>
                        {loading ? "Loading…" : "Refresh Report"}
                    </button>
                    <button type="button" className="rrep-primary" disabled={!printable} onClick={() => window.print()}>
                        Print / Save as PDF
                    </button>
                </div>
            </div>
            <div className="rrep-report-tabs" role="group" aria-label="Rental report type">
                <button
                    type="button"
                    className={reportType === "current" ? "rrep-tab selected" : "rrep-tab"}
                    aria-pressed={reportType === "current"}
                    onClick={() => setReportType("current")}
                >
                    Current Rentals
                </button>
                <button
                    type="button"
                    className={overdue ? "rrep-tab selected" : "rrep-tab"}
                    aria-pressed={overdue}
                    onClick={() => setReportType("overdue")}
                >
                    Overdue Rentals
                </button>
                <button
                    type="button"
                    className={isHistory ? "rrep-tab selected" : "rrep-tab"}
                    aria-pressed={isHistory}
                    onClick={() => setReportType("history")}
                >
                    Renter History
                </button>
                <button
                    type="button"
                    className={isIndividual ? "rrep-tab selected" : "rrep-tab"}
                    aria-pressed={isIndividual}
                    onClick={() => setReportType("individual")}
                >
                    Individual Rental
                </button>
            </div>
            {isRenterReport && (
                <div className="rrep-renter-filter">
                    <label htmlFor="rrep-renter-search">Search renter</label>
                    <div className="rrep-search-area">
                        <input
                            id="rrep-renter-search"
                            type="search"
                            value={renterSearch}
                            onChange={e => setRenterSearch(e.target.value)}
                            placeholder="Search by name, phone, email or renter ID"
                            autoComplete="off"
                        />
                        {renterSearch.trim() && (
                            <div className="rrep-search-results" role="listbox" aria-label="Matching renters">
                                {filteredCustomers.length ? filteredCustomers.map(customer => (
                                    <button
                                        key={customer.customerId}
                                        type="button"
                                        role="option"
                                        aria-selected={String(customer.customerId) === String(selectedCustomerId)}
                                        className="rrep-search-result"
                                        onClick={() => {
                                            setSelectedCustomerId(String(customer.customerId));
                                            setRenterSearch("");
                                        }}
                                    >
                                        <strong>{customer.customerName}</strong>
                                        <small>#{customer.customerId} · {customer.phone || "No phone"}</small>
                                    </button>
                                )) : <p className="rrep-search-empty">No matching renters found.</p>}
                            </div>
                        )}
                    </div>
                    <label htmlFor="rrep-renter-select">Selected renter</label>
                    <select
                        id="rrep-renter-select"
                        value={selectedCustomerId}
                        onChange={e => {
                            setSelectedCustomerId(e.target.value);
                            setRenterSearch("");
                        }}
                    >
                        <option value="">Choose a registered renter</option>
                        {customers.map(customer => (
                            <option key={customer.customerId} value={customer.customerId}>
                                {customer.customerName} — #{customer.customerId}
                            </option>
                        ))}
                    </select>
                    {isIndividual && <>
                        <label htmlFor="rrep-rental-id">Rental ID</label>
                        <select id="rrep-rental-id" value={selectedRentalId} onChange={e => setSelectedRentalId(e.target.value)}>
                            <option value="">Choose rental ID</option>
                            {history.map(record => <option key={record.rental.rentalId} value={record.rental.rentalId}>
                                Rental #{record.rental.rentalId} — {record.rental.rentalStatus || "—"}
                            </option>)}
                        </select>
                    </>}
                    <span>{isIndividual ? "Detailed report for one rental belonging to the selected renter." : "All rentals for the selected renter, including completed and cancelled rentals."}</span>
                </div>
            )}
            {error &&
                <div className="rrep-error" role="alert">{error}</div>
            }
            {loading &&
                <p role="status">Loading {isRenterReport ? "rental details" : `${reportType} rentals`}…</p>
            }
            {printable &&
                <div className="rrep-preview-wrap">
                <div className="rrep-preview-label">A4 report preview · {isIndividual ? "Individual rental" : isHistory ? "Renter history" : (overdue ? "Overdue" : "Current rentals")}</div>
                <article className="rrep-paper" id="rentflow-print-report">
                    <header className="rrep-paper-header">
                        <div>
                            <div className="rrep-brand">RentFlow</div>
                            <div className="rrep-subtitle">Equipment Hire &amp; Rental Management System</div>
                        </div>
                        <div className="rrep-meta">
                            <strong>Company ID: {companyId}</strong>
                            <div>Generated: {generatedAt.toLocaleString()}</div>
                        </div>
                    </header>
                    <h2>{reportTitle}</h2>
                    {isRenterReport && <div className="rrep-history-person"><strong>{selectedCustomer?.customerName || `Renter #${selectedCustomerId}`}</strong><div>Customer ID: {selectedCustomerId} · Phone: {selectedCustomer?.phone || "—"} · Email: {selectedCustomer?.email || "—"}</div></div>}
                    <p className="rrep-description">{isIndividual ? "Complete details of the selected rental and any available settlement information." : isHistory ? "Complete rental history for the selected renter, ordered by Rental ID." : overdue
                        ? "Rentals identified as overdue by the rental management system. Days overdue are calculated as of report generation."
                        : "Rentals currently marked active in the rental management system."}</p>
                    {!isIndividual && <div className="rrep-count"><span>Total {isHistory ? "historical" : (overdue ? "overdue" : "current")} rentals</span><strong>{isHistory ? history.length : rentals.length}</strong></div>}
                    {isIndividual ? (
                        individualRecord ? <>
                            <div className="rrep-detail-grid">
                                <div>
                                    <span>Rental ID</span>
                                    <strong>#{individualRecord.rental.rentalId}</strong>
                                </div>
                                <div><span>Status</span>
                                    <strong>{individualRecord.rental.rentalStatus || "—"}</strong>
                                </div>
                                <div>
                                    <span>Rental Date</span>
                                    <strong>{dateText(individualRecord.rental.rentalDate)}</strong>
                                </div>
                                <div>
                                    <span>Start Date</span>
                                    <strong>{dateText(individualRecord.rental.startDate)}</strong>
                                </div>
                                <div>
                                    <span>Due Date</span>
                                    <strong>{dateText(individualRecord.rental.dueDate)}</strong>
                                </div>
                                <div>
                                    <span>Agreed Security Deposit</span>
                                    <strong>{currency(individualRecord.totalDeposit)}</strong>
                                </div>
                            </div>
                            <h3 className="rrep-section-title">Rented Equipment</h3>
                            <table className="rrep-table">
                                <thead>
                                <tr>
                                    <th>Equipment Name</th>
                                    <th>Quantity</th>
                                    <th>Rate / Unit</th>
                                    <th>Rate Period</th>
                                    <th>Line Deposit</th>
                                </tr>
                                </thead>
                                <tbody>{(individualRecord.rentalItems || []).length ? individualRecord.rentalItems.map((item, index) =>
                                    <tr key={item.rentalItemId ?? index}>
                                        <td>{equipmentNames[String(item.equipmentId)] || `Equipment #${item.equipmentId}`}</td>
                                        <td>{item.quantity}</td>
                                        <td>{currency(item.ratePerUnit ?? item.rentalRate)}</td>
                                        <td>{item.ratePeriod || "—"}</td>
                                        <td>{currency(item.lineDeposit)}</td>
                                    </tr>) : <tr><td colSpan="5" className="rrep-empty">No rental items recorded.</td></tr>}
                                </tbody>
                            </table>
                            <h3 className="rrep-section-title">Notes</h3>
                            <p className="rrep-notes">{displayNotes(individualRecord.rental.notes)}</p>
                            <h3 className="rrep-section-title">Financial Settlement</h3>
                            {settlement ? <div className="rrep-finances">
                                {[ ["Rental Charges", "rentalCharges"], ["Late Charges", "lateCharges"],
                                    ["Damage Charges", "damageCharges"], ["Lost Item Charges", "lostItemCharges"],
                                    ["Total Charges", "totalCharges"], ["Already Paid", "alreadyPaid"],
                                    ["Deposit Held", "depositHeld"], ["Deposit Applied", "depositToUse"],
                                    ["Deposit to Refund", "depositToRefund"], ["Outstanding", "outstanding"],
                                    ["Balance After Deposit", "balanceAfterDeposit"] ].map(([label, field]) =>
                                    <div key={field}><span>{label}</span><strong>{currency(settlement[field])}</strong></div>)}
                            </div> : <p className="rrep-notes">{settlementMessage || "Checking available settlement information…"}</p>}
                        </> : <p className="rrep-empty">Select a renter and rental ID to generate an individual rental report.</p>
                    ) : isHistory ? (
                        <table className="rrep-table">
                            <thead><tr><th>Rental ID</th><th>Rental Date</th><th>Start</th><th>Due</th><th>Status</th><th>Item Count</th><th>Agreed Deposit (Rs.)</th></tr></thead>
                            <tbody>{history.length === 0 ? <tr><td className="rrep-empty" colSpan="7">No rentals found for this renter.</td></tr> :
                                history.map(record => <tr key={record.rental.rentalId}>
                                    <td>#{record.rental.rentalId}</td>
                                    <td>{dateText(record.rental.rentalDate)}</td>
                                    <td>{dateText(record.rental.startDate)}</td>
                                    <td>{dateText(record.rental.dueDate)}</td>
                                    <td>{record.rental.rentalStatus || "—"}</td>
                                    <td>{(record.rentalItems || []).length}</td>
                                    <td>{record.totalDeposit == null ? "—" : Number(record.totalDeposit).toLocaleString()}</td>
                                </tr>)}
                            </tbody>
                        </table>
                    ) : (
                        <table className="rrep-table">
                            <thead><tr><th>Rental ID</th><th>Renter</th><th>Phone</th><th>Start</th><th>Due</th>{overdue && <th>Days Late</th>}<th>Status</th></tr></thead>
                            <tbody>
                            {rentals.length === 0 ? <tr><td colSpan={overdue ? 7 : 6} className="rrep-empty">No {overdue ? "overdue" : "current"} rentals found.</td></tr> :
                                rentals.map(r => {
                                    const renter = customerMap.get(Number(r.customerId));
                                    return <tr key={r.rentalId}>
                                        <td>{r.rentalId}</td>
                                        <td>{renter?.customerName || `Customer #${r.customerId}`}</td>
                                        <td>{renter?.phone || "—"}</td>
                                        <td>{dateText(r.startDate)}</td>
                                        <td>{dateText(r.dueDate)}</td>{overdue && <td>{daysOverdue(r.dueDate)}</td>}<td>{r.rentalStatus || "—"}</td>
                                    </tr>;
                                })}
                            </tbody>
                        </table>
                    )}
                    <footer className="rrep-paper-footer">Generated by RentFlow · {isIndividual ? "Individual Rental" : isHistory ? "Renter Rental History" : (overdue ? "Overdue" : "Current Rentals")} Report · Data as of {generatedAt.toLocaleString()}</footer>
                </article>
                <p className="rrep-help">Click <strong>Print / Save as PDF</strong>. In the browser print dialog, choose a printer, or select <strong>Save as PDF</strong> as the destination.</p>
            </div>}
        </main>
    );
}
