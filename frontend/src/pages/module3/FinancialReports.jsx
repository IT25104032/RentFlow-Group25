import React, { useState } from 'react';
import { getRentalSummary, getReport } from '../../services/module3/reportApi';
import '../../styles/module3/PaymentManagement.css';

const REPORT_TYPES = [
    { value: 'OUTSTANDING', label: 'Outstanding Invoices' },
    { value: 'OVERDUE', label: 'Overdue Invoices' },
    { value: 'PAYMENTS_BY_METHOD', label: 'Payments by Method' },
    { value: 'REVENUE', label: 'Revenue for Period' }
];

const money = (value) => `Rs. ${Number(value || 0).toFixed(2)}`;
const formatDateTime = (value) => (value ? value.replace('T', ' ').substring(0, 16) : '-');
const escapeHtml = (value) => String(value ?? '-')
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

const getBadgeClass = (status) => {
    if (status === 'PAID' || status === 'COMPLETED') return 'badge-paid';
    if (status === 'PARTIALLY_PAID') return 'badge-partial';
    return 'badge-unpaid';
};

// Opens the given HTML in a new window and prints it
const printHtml = (title, bodyHtml) => {
    const printWindow = window.open('', '_blank');
    printWindow.document.write(`
        <html>
        <head>
            <title>${escapeHtml(title)}</title>
            <style>
                body { font-family: Arial, sans-serif; padding: 40px; color: #333; }
                h1 { font-size: 26px; color: #0056b3; margin-bottom: 4px; }
                h2 { font-size: 18px; margin-top: 28px; }
                .sub { color: #666; margin-bottom: 20px; }
                table { width: 100%; border-collapse: collapse; margin-top: 10px; }
                th, td { border: 1px solid #ddd; padding: 8px; text-align: left; font-size: 13px; }
                th { background-color: #f8f9fa; }
                .total td { font-weight: bold; background-color: #f8f9fa; }
            </style>
        </head>
        <body>
            <h1>RentFlow</h1>
            ${bodyHtml}
            <script>
                window.onload = function() { window.print(); setTimeout(() => window.close(), 500); };
            </script>
        </body>
        </html>
    `);
    printWindow.document.close();
};

// Downloads rows as a CSV file the user can open in Excel
const exportCsv = (fileName, headers, rows) => {
    const quote = (value) => `"${String(value ?? '').replace(/"/g, '""')}"`;
    const lines = [headers.map(quote).join(','), ...rows.map(row => row.map(quote).join(','))];
    const blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = fileName;
    link.click();
    URL.revokeObjectURL(link.href);
};

export default function FinancialReports() {
    // Rental summary
    const [rentalIdInput, setRentalIdInput] = useState('');
    const [summary, setSummary] = useState(null);
    const [summaryError, setSummaryError] = useState(null);
    const [summaryLoading, setSummaryLoading] = useState(false);

    // Company reports
    const [reportType, setReportType] = useState('OUTSTANDING');
    const [fromDate, setFromDate] = useState('');
    const [toDate, setToDate] = useState('');
    const [report, setReport] = useState(null);
    const [reportError, setReportError] = useState(null);
    const [reportLoading, setReportLoading] = useState(false);

    const handleViewSummary = async (e) => {
        e.preventDefault();
        setSummaryLoading(true);
        setSummaryError(null);
        setSummary(null);
        try {
            setSummary(await getRentalSummary(rentalIdInput));
        } catch (err) {
            setSummaryError(err.message);
        } finally {
            setSummaryLoading(false);
        }
    };

    const handleRunReport = async (e) => {
        e.preventDefault();
        setReportLoading(true);
        setReportError(null);
        setReport(null);
        try {
            setReport(await getReport(reportType, fromDate, toDate));
        } catch (err) {
            setReportError(err.message);
        } finally {
            setReportLoading(false);
        }
    };

    const periodText = (r) => {
        if (r.from && r.to) return `${r.from} to ${r.to}`;
        if (r.from) return `From ${r.from}`;
        if (r.to) return `Up to ${r.to}`;
        return 'All dates';
    };

    const printSummary = () => {
        const s = summary;
        const chargeRows = s.charges.map(c => `
            <tr><td>${escapeHtml(c.chargeType)}</td><td>${escapeHtml(c.description)}</td><td>${money(c.amount)}</td></tr>`).join('');
        const paymentRows = s.payments.map(p => `
            <tr><td>${formatDateTime(p.paymentDate)}</td><td>${money(p.amount)}</td><td>${escapeHtml(p.paymentMethod)}</td>
                <td>${escapeHtml(p.referenceNo)}</td><td>${escapeHtml(p.receivedByName)}</td><td>${escapeHtml(p.paymentStatus)}</td></tr>`).join('');

        printHtml(`Rental #${s.rentalId} Financial Summary`, `
            <div class="sub">Rental Financial Summary</div>
            <p><strong>Rental #:</strong> ${s.rentalId} &nbsp; <strong>Customer:</strong> ${escapeHtml(s.customerName)}<br>
               <strong>Rental period:</strong> ${s.startDate} to ${s.dueDate} &nbsp; <strong>Invoice #:</strong> ${s.invoiceId} (${s.invoiceStatus})</p>
            <table>
                <tr><td>Rental Charges</td><td>${money(s.rentalCharges)}</td></tr>
                <tr><td>Additional Charges</td><td>${money(s.additionalCharges)}</td></tr>
                <tr><td>Security Deposit Deduction</td><td>- ${money(s.depositDeduction)}</td></tr>
                <tr class="total"><td>Total Payable</td><td>${money(s.totalPayable)}</td></tr>
                <tr><td>Amount Paid</td><td>${money(s.amountPaid)}</td></tr>
                <tr class="total"><td>Outstanding Balance</td><td>${money(s.balanceDue)}</td></tr>
            </table>
            <h2>Charges</h2>
            <table><tr><th>Type</th><th>Description</th><th>Amount</th></tr>
                ${chargeRows || '<tr><td colspan="3">No charges.</td></tr>'}</table>
            <h2>Payment History</h2>
            <table><tr><th>Date</th><th>Amount</th><th>Method</th><th>Reference</th><th>Received By</th><th>Status</th></tr>
                ${paymentRows || '<tr><td colspan="6">No payments recorded.</td></tr>'}</table>
        `);
    };

    const printReport = () => {
        const r = report;
        let table;
        if (r.reportType === 'PAYMENTS_BY_METHOD') {
            table = `
                <table><tr><th>Method</th><th>No. of Payments</th><th>Total</th></tr>
                ${r.methodTotals.map(m => `<tr><td>${m.paymentMethod}</td><td>${m.count}</td><td>${money(m.total)}</td></tr>`).join('')}
                <tr class="total"><td>Total Paid</td><td>${r.recordCount}</td><td>${money(r.totalPaid)}</td></tr></table>
                <h2>Payments</h2>
                <table><tr><th>Date</th><th>Invoice #</th><th>Customer</th><th>Method</th><th>Reference</th><th>Amount</th></tr>
                ${r.payments.map(p => `<tr><td>${formatDateTime(p.paymentDate)}</td><td>${p.invoiceId}</td><td>${escapeHtml(p.customerName)}</td>
                    <td>${p.paymentMethod}</td><td>${escapeHtml(p.referenceNo)}</td><td>${money(p.amount)}</td></tr>`).join('')}</table>`;
        } else {
            table = `
                <table><tr><th>Invoice #</th><th>Rental</th><th>Customer</th><th>Invoice Date</th><th>Due Date</th>
                    <th>Total</th><th>Paid</th><th>Balance</th><th>Status</th>${r.reportType === 'OVERDUE' ? '<th>Days Overdue</th>' : ''}</tr>
                ${r.invoices.map(i => `<tr><td>${i.invoiceId}</td><td>${i.rentalId}</td><td>${escapeHtml(i.customerName)}</td>
                    <td>${i.invoiceDate}</td><td>${i.dueDate ?? '-'}</td><td>${money(i.totalAmount)}</td><td>${money(i.amountPaid)}</td>
                    <td>${money(i.balanceDue)}</td><td>${i.invoiceStatus}</td>${r.reportType === 'OVERDUE' ? `<td>${i.daysOverdue}</td>` : ''}</tr>`).join('')}
                <tr class="total"><td colspan="5">Totals (${r.recordCount} invoices)</td><td>${money(r.totalInvoiced)}</td>
                    <td>${money(r.totalPaid)}</td><td>${money(r.totalOutstanding)}</td><td colspan="${r.reportType === 'OVERDUE' ? 2 : 1}"></td></tr></table>`;
        }
        printHtml(r.title, `<div class="sub">${r.title} | ${periodText(r)}</div>${table}`);
    };

    const exportReport = () => {
        const r = report;
        const fileName = `${r.reportType.toLowerCase()}-report.csv`;
        if (r.reportType === 'PAYMENTS_BY_METHOD') {
            exportCsv(fileName,
                ['Payment #', 'Date', 'Invoice #', 'Rental #', 'Customer', 'Method', 'Reference', 'Received By', 'Amount'],
                r.payments.map(p => [p.paymentId, formatDateTime(p.paymentDate), p.invoiceId, p.rentalId, p.customerName,
                    p.paymentMethod, p.referenceNo, p.receivedByName, Number(p.amount).toFixed(2)]));
        } else {
            exportCsv(fileName,
                ['Invoice #', 'Rental #', 'Customer', 'Invoice Date', 'Due Date', 'Total', 'Paid', 'Balance', 'Status', 'Days Overdue'],
                r.invoices.map(i => [i.invoiceId, i.rentalId, i.customerName, i.invoiceDate, i.dueDate,
                    Number(i.totalAmount).toFixed(2), Number(i.amountPaid).toFixed(2), Number(i.balanceDue).toFixed(2),
                    i.invoiceStatus, i.daysOverdue]));
        }
    };


    const inputStyle = { padding: '8px 12px', borderRadius: '4px', border: '1px solid #ccc' };
    const sectionStyle = { background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)', marginBottom: '30px' };
    const totalRow = { fontWeight: 'bold', backgroundColor: '#f4f6f8' };

    return (
        <div className="payment-container">
            <div className="header-bar">
                <h2>Financial Summary & Reports</h2>
            </div>

            {/* ===== Scenario A: Rental Financial Summary ===== */}
            <div style={sectionStyle}>
                <div className="header-bar" style={{ marginBottom: '10px' }}>
                    <h3 style={{ margin: 0 }}>Rental Financial Summary</h3>
                    <form onSubmit={handleViewSummary} style={{ display: 'flex', gap: '10px' }}>
                        <input type="number" placeholder="Rental ID" value={rentalIdInput}
                               onChange={(e) => setRentalIdInput(e.target.value)} style={{ ...inputStyle, width: '120px' }} required />
                        <button type="submit" className="btn-submit" disabled={summaryLoading}>
                            {summaryLoading ? 'Loading...' : 'View Summary'}
                        </button>
                    </form>
                </div>

                {summaryError && <div className="error-banner">{summaryError}</div>}

                {summary && (
                    <>
                        <p style={{ color: '#555' }}>
                            <strong>Rental #{summary.rentalId}</strong> | Customer: {summary.customerName || '-'}
                            {summary.customerPhone ? ` (${summary.customerPhone})` : ''} | Period: {summary.startDate} to {summary.dueDate}
                            {' '}| Invoice #{summary.invoiceId}{' '}
                            <span className={`badge ${getBadgeClass(summary.invoiceStatus)}`}>{summary.invoiceStatus}</span>
                        </p>

                        <div style={{ display: 'flex', gap: '30px', flexWrap: 'wrap' }}>
                            <table className="invoice-table" style={{ maxWidth: '420px' }}>
                                <tbody>
                                <tr><td>Rental Charges</td><td style={{ textAlign: 'right' }}>{money(summary.rentalCharges)}</td></tr>
                                <tr><td>Additional Charges</td><td style={{ textAlign: 'right' }}>{money(summary.additionalCharges)}</td></tr>
                                <tr><td>Security Deposit Deduction</td><td style={{ textAlign: 'right' }}>- {money(summary.depositDeduction)}</td></tr>
                                <tr style={totalRow}><td>Total Payable</td><td style={{ textAlign: 'right' }}>{money(summary.totalPayable)}</td></tr>
                                <tr><td>Amount Paid</td><td style={{ textAlign: 'right' }}>{money(summary.amountPaid)}</td></tr>
                                <tr style={totalRow}><td>Outstanding Balance</td><td style={{ textAlign: 'right' }}>{money(summary.balanceDue)}</td></tr>
                                </tbody>
                            </table>

                            <table className="invoice-table" style={{ maxWidth: '360px' }}>
                                <tbody>
                                <tr style={totalRow}><td colSpan="2">Security Deposit</td></tr>
                                {summary.deposit ? (
                                    <>
                                        <tr><td>Required</td><td style={{ textAlign: 'right' }}>{money(summary.deposit.calculated)}</td></tr>
                                        <tr><td>Received</td><td style={{ textAlign: 'right' }}>{money(summary.deposit.received)}</td></tr>
                                        <tr><td>Deducted</td><td style={{ textAlign: 'right' }}>{money(summary.deposit.deducted)}</td></tr>
                                        <tr><td>Refunded</td><td style={{ textAlign: 'right' }}>{money(summary.deposit.refunded)}</td></tr>
                                        <tr><td>Status</td><td style={{ textAlign: 'right' }}>{summary.deposit.status}</td></tr>
                                    </>
                                ) : (
                                    <tr><td colSpan="2">No deposit recorded for this rental.</td></tr>
                                )}
                                </tbody>
                            </table>
                        </div>

                        <h4 style={{ marginTop: '25px' }}>Charges</h4>
                        <table className="invoice-table">
                            <thead><tr><th>Type</th><th>Description</th><th>Amount</th></tr></thead>
                            <tbody>
                            {summary.charges.length === 0 ? (
                                <tr><td colSpan="3">No charges.</td></tr>
                            ) : summary.charges.map((c, idx) => (
                                <tr key={idx}><td>{c.chargeType}</td><td>{c.description || '-'}</td><td>{money(c.amount)}</td></tr>
                            ))}
                            </tbody>
                        </table>

                        <h4 style={{ marginTop: '25px' }}>Payment History</h4>
                        <table className="invoice-table">
                            <thead><tr><th>Date</th><th>Amount</th><th>Method</th><th>Reference</th><th>Received By</th><th>Status</th></tr></thead>
                            <tbody>
                            {summary.payments.length === 0 ? (
                                <tr><td colSpan="6">No payments recorded.</td></tr>
                            ) : summary.payments.map(p => (
                                <tr key={p.paymentId}>
                                    <td>{formatDateTime(p.paymentDate)}</td><td>{money(p.amount)}</td><td>{p.paymentMethod}</td>
                                    <td>{p.referenceNo || '-'}</td><td>{p.receivedByName || '-'}</td>
                                    <td><span className={`badge ${getBadgeClass(p.paymentStatus)}`}>{p.paymentStatus}</span></td>
                                </tr>
                            ))}
                            </tbody>
                        </table>

                        <div style={{ textAlign: 'right', marginTop: '15px' }}>
                            <button className="btn-print" onClick={printSummary}>🖨️ Print Summary</button>
                        </div>
                    </>
                )}
            </div>

            {/* ===== Scenario B: Billing, Payment and Revenue Reports ===== */}
            <div style={sectionStyle}>
                <h3 style={{ marginTop: 0 }}>Billing, Payment & Revenue Reports</h3>
                <form onSubmit={handleRunReport} style={{ display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap', marginBottom: '15px' }}>
                    <select value={reportType} onChange={(e) => setReportType(e.target.value)} style={inputStyle}>
                        {REPORT_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
                    </select>
                    <label>From <input type="date" value={fromDate} onChange={(e) => setFromDate(e.target.value)} style={inputStyle} /></label>
                    <label>To <input type="date" value={toDate} onChange={(e) => setToDate(e.target.value)} style={inputStyle} /></label>
                    <button type="submit" className="btn-submit" disabled={reportLoading}>
                        {reportLoading ? 'Running...' : 'Run Report'}
                    </button>
                </form>

                {reportError && <div className="error-banner">{reportError}</div>}

                {report && (
                    <>
                        <p style={{ color: '#555' }}><strong>{report.title}</strong> | {periodText(report)} | {report.recordCount} record(s)</p>

                        {report.recordCount === 0 ? (
                            <div className="error-banner" style={{ backgroundColor: '#fff3cd', color: '#856404' }}>
                                No records found for these filters. Try a different report type or date range.
                            </div>
                        ) : report.reportType === 'PAYMENTS_BY_METHOD' ? (
                            <>
                                <table className="invoice-table" style={{ maxWidth: '500px' }}>
                                    <thead><tr><th>Method</th><th>No. of Payments</th><th>Total</th></tr></thead>
                                    <tbody>
                                    {report.methodTotals.map(m => (
                                        <tr key={m.paymentMethod}><td>{m.paymentMethod}</td><td>{m.count}</td><td>{money(m.total)}</td></tr>
                                    ))}
                                    <tr style={totalRow}><td>Total Paid</td><td>{report.recordCount}</td><td>{money(report.totalPaid)}</td></tr>
                                    </tbody>
                                </table>

                                <h4 style={{ marginTop: '25px' }}>Payments</h4>
                                <table className="invoice-table">
                                    <thead><tr><th>Date</th><th>Invoice #</th><th>Rental</th><th>Customer</th><th>Method</th><th>Reference</th><th>Received By</th><th>Amount</th></tr></thead>
                                    <tbody>
                                    {report.payments.map(p => (
                                        <tr key={p.paymentId}>
                                            <td>{formatDateTime(p.paymentDate)}</td><td>#{p.invoiceId}</td><td>{p.rentalId}</td>
                                            <td>{p.customerName || '-'}</td><td>{p.paymentMethod}</td><td>{p.referenceNo || '-'}</td>
                                            <td>{p.receivedByName || '-'}</td><td>{money(p.amount)}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </>
                        ) : (
                            <table className="invoice-table">
                                <thead>
                                <tr>
                                    <th>Invoice #</th><th>Rental</th><th>Customer</th><th>Invoice Date</th><th>Due Date</th>
                                    <th>Total</th><th>Paid</th><th>Balance</th><th>Status</th>
                                    {report.reportType === 'OVERDUE' && <th>Days Overdue</th>}
                                </tr>
                                </thead>
                                <tbody>
                                {report.invoices.map(i => (
                                    <tr key={i.invoiceId}>
                                        <td>#{i.invoiceId}</td><td>{i.rentalId}</td><td>{i.customerName || '-'}</td>
                                        <td>{i.invoiceDate}</td><td>{i.dueDate || '-'}</td>
                                        <td>{money(i.totalAmount)}</td><td>{money(i.amountPaid)}</td><td>{money(i.balanceDue)}</td>
                                        <td><span className={`badge ${getBadgeClass(i.invoiceStatus)}`}>{i.invoiceStatus}</span></td>
                                        {report.reportType === 'OVERDUE' && <td>{i.daysOverdue}</td>}
                                    </tr>
                                ))}
                                <tr style={totalRow}>
                                    <td colSpan="5">Totals</td>
                                    <td>{money(report.totalInvoiced)}</td><td>{money(report.totalPaid)}</td><td>{money(report.totalOutstanding)}</td>
                                    <td colSpan={report.reportType === 'OVERDUE' ? 2 : 1}></td>
                                </tr>
                                </tbody>
                            </table>
                        )}

                        {report.recordCount > 0 && (
                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '15px' }}>
                                <button className="btn-print" onClick={printReport}>🖨️ Print</button>
                                <button className="btn-pay" onClick={exportReport}>⬇ Export CSV</button>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}
