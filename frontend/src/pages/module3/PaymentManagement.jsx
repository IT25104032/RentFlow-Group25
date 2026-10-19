import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getInvoices, getChargesByInvoice, previewInvoice, previewExtension } from '../../services/module3/paymentApi';
import InvoicePreviewModal from '../../components/module3/InvoicePreviewModal';
import PaymentModal from '../../components/module3/PaymentModal';
import '../../styles/module3/PaymentManagement.css';
import PaymentHistoryModal from '../../components/module3/PaymentHistoryModal';
import ExtensionUpdateModal from '../../components/module3/ExtensionUpdateModal';
import BillingTabs from '../../components/module3/BillingTabs';

export default function PaymentManagement() {
    const navigate = useNavigate();
    const [invoices, setInvoices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedInvoice, setSelectedInvoice] = useState(null);
    const [invoicePreview, setInvoicePreview] = useState(null);
    const [historyInvoice, setHistoryInvoice] = useState(null);
    const [extensionPreview, setExtensionPreview] = useState(null);
    // State for generating invoice
    const [rentalIdInput, setRentalIdInput] = useState('');
    const [generating, setGenerating] = useState(false);

    const fetchList = async () => {
        setLoading(true);
        try {
            const data = await getInvoices();
            setInvoices(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchList(); }, []);

    const handleGenerateInvoice = async (e) => {
        e.preventDefault();
        if (!rentalIdInput.trim()) return;

        setGenerating(true);
        setError(null);
        try {
            const preview = await previewInvoice(rentalIdInput.trim());
            setInvoicePreview(preview); // opens the preview modal
        } catch (err) {
            setError(err.message || 'Error generating invoice');
        } finally {
            setGenerating(false);
        }
    };

    const handleExtension = async (inv) => {
        setError(null);
        try {
            setExtensionPreview(await previewExtension(inv.invoiceId));
        } catch (err) {
            setError(err.message);
        }
    };

    const getBadgeClass = (status) => {
        if (status === 'PAID') return 'badge-paid';
        if (status === 'PARTIALLY_PAID') return 'badge-partial';
        return 'badge-unpaid';
    };

    const handlePrint = async (inv) => {
        try {
            const charges = await getChargesByInvoice(inv.invoiceId, inv.rentalId);

            const chargeRows = charges.map(charge => `
            <tr>
                <td>${charge.chargeType} ${charge.chargeDescription ? `- ${charge.chargeDescription}` : ''}</td>
                <td>Rs. ${Number(charge.amount).toFixed(2)}</td>
            </tr>
        `).join('');

            const printWindow = window.open('', '_blank');
            printWindow.document.write(`
            <html>
            <head>
                <title>Invoice #${inv.invoiceId}</title>
                <style>
                    body { font-family: Arial, sans-serif; padding: 40px; color: #333; }
                    .header { font-size: 28px; font-weight: bold; margin-bottom: 5px; color: #0056b3; }
                    .sub-header { font-size: 16px; color: #666; margin-bottom: 30px; }
                    .details { margin-bottom: 20px; line-height: 1.6; }
                    table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                    th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
                    th { background-color: #f8f9fa; }
                    .summary-row td { background-color: #f8f9fa; font-weight: bold; }
                    .status { font-weight: bold; font-size: 18px; margin-top: 20px; text-align: right; }
                </style>
            </head>
            <body>
                <div class="header">RentFlow</div>
                <div class="sub-header">Official Itemized Receipt</div>
                
                <div class="details">
                    <strong>Invoice #:</strong> ${inv.invoiceId}<br>
                    <strong>Rental ID:</strong> ${inv.rentalId}<br>
                    <strong>Date:</strong> ${inv.invoiceDate}<br>
                    <strong>Due Date:</strong> ${inv.dueDate}
                </div>

                <table>
                    <thead>
                        <tr>
                            <th>Charge Description</th>
                            <th>Amount</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${chargeRows.length > 0 ? chargeRows : '<tr><td colspan="2">No itemized charges found.</td></tr>'}
                        ${Number(inv.depositDeduction) > 0 ? `
                        <tr>
                            <td style="text-align: right;">Security Deposit Deduction:</td>
                            <td>- Rs. ${Number(inv.depositDeduction).toFixed(2)}</td>
                        </tr>` : ''}
                        <tr class="summary-row">
                            <td style="text-align: right;">Total Amount:</td>
                            <td>Rs. ${Number(inv.totalAmount).toFixed(2)}</td>
                        </tr>
                        <tr>
                            <td style="text-align: right;">Amount Paid:</td>
                            <td>Rs. ${Number(inv.amountPaid).toFixed(2)}</td>
                        </tr>
                        <tr class="summary-row">
                            <td style="text-align: right;">Balance Due:</td>
                            <td>Rs. ${Number(inv.balanceDue).toFixed(2)}</td>
                        </tr>
                    </tbody>
                </table>
                
                <div class="status">Status: ${inv.invoiceStatus}</div>
                
                <script>
                    window.onload = function() {
                        window.print();
                        setTimeout(() => window.close(), 500);
                    };
                </script>
            </body>
            </html>
        `);
            printWindow.document.close();
        } catch (error) {
            console.error("Failed to fetch charges for printing:", error);
            alert("Could not load itemized details for this receipt.");
        }
    };

    // ---- Presentation-only summary figures derived from the loaded invoices ----
    const num = (v) => Number(v) || 0;
    const fmtMoney = (v) => num(v).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    const todayStr = new Date().toISOString().slice(0, 10);
    const totalOutstanding = invoices.reduce((sum, inv) => sum + num(inv.balanceDue), 0);
    const openInvoices = invoices.filter((inv) => num(inv.balanceDue) > 0).length;
    const totalInvoiced = invoices.reduce((sum, inv) => sum + num(inv.totalAmount), 0);
    const totalCollected = invoices.reduce((sum, inv) => sum + num(inv.amountPaid), 0);
    const collectedPct = totalInvoiced > 0 ? Math.round((totalCollected / totalInvoiced) * 100) : 0;
    const overdueCount = invoices.filter(
        (inv) => num(inv.balanceDue) > 0 && inv.dueDate && String(inv.dueDate).slice(0, 10) < todayStr
    ).length;
    const paidCount = invoices.filter((inv) => inv.invoiceStatus === 'PAID').length;
    const partialCount = invoices.filter((inv) => inv.invoiceStatus === 'PARTIALLY_PAID').length;
    const unpaidCount = invoices.filter((inv) => inv.invoiceStatus === 'UNPAID').length;
    const statusSlices = [
        { label: 'Paid', count: paidCount, color: '#0B63F6' },
        { label: 'Partially paid', count: partialCount, color: '#12C8E8' },
        { label: 'Unpaid', count: unpaidCount, color: '#A78BFA' }
    ];
    const donutTotal = statusSlices.reduce((sum, slice) => sum + slice.count, 0);
    const DONUT_R = 54;
    const DONUT_C = 2 * Math.PI * DONUT_R;
    let donutOffset = 0;

    return (
        <div className="payment-container">
            <div className="rf-page-head">
                <h1>Billing &amp; Payments</h1>
                <BillingTabs />
            </div>

            <section className="rf-summary">
                <div className="rf-hero">
                    <svg className="rf-hero-icon" viewBox="0 0 64 64" fill="none" stroke="#fff" strokeWidth="3.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                        <path d="M16 6h22l14 14v34a4 4 0 0 1-4 4H16a4 4 0 0 1-4-4V10a4 4 0 0 1 4-4z" />
                        <path d="M38 6v14h14" />
                        <path d="M22 32h20M22 41h20M22 50h12" />
                    </svg>
                    <div className="rf-hero-label">Total outstanding</div>
                    <div className="rf-hero-amount">{fmtMoney(totalOutstanding)}<sup>Rs</sup></div>
                    <div className="rf-hero-sub">Across {openInvoices} open invoice{openInvoices === 1 ? '' : 's'}</div>
                    <button type="button" className="rf-hero-button" onClick={() => navigate('/financial-reports')}>View reports</button>
                </div>

                <div className="rf-stack">
                    <div className="rf-stat">
                        <span className="rf-chip">{collectedPct}%</span>
                        <div className="rf-stat-label">Collected</div>
                        <div className="rf-stat-value">{fmtMoney(totalCollected)}<sup>Rs</sup></div>
                        <div className="rf-stat-sub">of {fmtMoney(totalInvoiced)} invoiced</div>
                        <svg className="rf-stat-icon" viewBox="0 0 34 34" fill="none" stroke="#8FB4F5" strokeWidth="3" strokeLinecap="round" aria-hidden="true">
                            <path d="M5 29V19M13 29V12M21 29V16M29 29V6" />
                        </svg>
                    </div>
                    <div className="rf-stat">
                        <span className="rf-chip danger">Action needed</span>
                        <div className="rf-stat-label">Overdue</div>
                        <div className="rf-stat-value">{overdueCount}</div>
                        <div className="rf-stat-sub">invoices past due date</div>
                        <svg className="rf-stat-icon" viewBox="0 0 34 34" fill="none" stroke="#F4A3AE" strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                            <path d="M17 4L31 29H3L17 4z" />
                            <path d="M17 13v8M17 25v.5" />
                        </svg>
                    </div>
                </div>

                <div className="rf-status-card">
                    <h3>Invoice status</h3>
                    <div className="rf-status-body">
                        <div className="rf-donut">
                            <svg viewBox="0 0 140 140" aria-hidden="true">
                                <circle cx="70" cy="70" r={DONUT_R} fill="none" stroke="#E6EEF9" strokeWidth="20" />
                                {donutTotal > 0 && statusSlices.map((slice) => {
                                    if (slice.count === 0) return null;
                                    const len = (slice.count / donutTotal) * DONUT_C;
                                    const circle = (
                                        <circle
                                            key={slice.label}
                                            cx="70" cy="70" r={DONUT_R}
                                            fill="none"
                                            stroke={slice.color}
                                            strokeWidth="20"
                                            strokeDasharray={`${Math.max(len - 2, 0)} ${DONUT_C - Math.max(len - 2, 0)}`}
                                            strokeDashoffset={-donutOffset}
                                        />
                                    );
                                    donutOffset += len;
                                    return circle;
                                })}
                            </svg>
                            <div className="rf-donut-center">
                                <small>Total</small>
                                <strong>{invoices.length}</strong>
                            </div>
                        </div>
                        <div className="rf-legend">
                            {statusSlices.map((slice) => (
                                <div className="rf-legend-row" key={slice.label}>
                                    <i style={{ background: slice.color }} />
                                    {slice.label}
                                    <b>{slice.count}</b>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </section>

            <div className="rf-section-head">
                <h2>Billing &amp; Payment Management</h2>

                {/* INVOICE GENERATION BAR */}
                <form className="rf-toolbar" onSubmit={handleGenerateInvoice}>
                    <input
                        type="number"
                        placeholder="Enter Rental ID"
                        value={rentalIdInput}
                        onChange={(e) => setRentalIdInput(e.target.value)}
                        required
                    />
                    <button
                        type="submit"
                        className="rf-btn-primary"
                        disabled={generating}
                    >
                        {generating ? 'Generating...' : '+ Generate Invoice'}
                    </button>
                    <button type="button" className="btn-refresh" onClick={fetchList}>↻ Refresh</button>
                </form>
            </div>

            {error && <div className="error-banner">{error}</div>}

            {!loading && (
                <div className="rf-table-card">
                    <div className="rf-table-scroll">
                        <table className="invoice-table">
                            <thead>
                            <tr>
                                <th>Invoice #</th>
                                <th>Rental ID</th>
                                <th>Invoice Date</th>
                                <th>Due Date</th>
                                <th>Subtotal</th>
                                <th>Total</th>
                                <th>Amount Paid</th>
                                <th>Balance Due</th>
                                <th>Status</th>
                                <th className="action-header">Action</th>
                            </tr>
                            </thead>
                            <tbody>
                            {invoices.map((inv) => (
                                <tr key={inv.invoiceId}>
                                    <td>#{inv.invoiceId}</td>
                                    <td>{inv.rentalId}</td>
                                    <td>{inv.invoiceDate}</td>
                                    <td>{inv.dueDate}</td>
                                    <td>Rs. {Number(inv.subtotal).toFixed(2)}</td>
                                    <td>Rs. {Number(inv.totalAmount).toFixed(2)}</td>
                                    <td>Rs. {Number(inv.amountPaid).toFixed(2)}</td>
                                    <td className={inv.balanceDue > 0 ? 'rf-amount-due' : 'rf-amount-clear'}>
                                        Rs. {Number(inv.balanceDue).toFixed(2)}
                                    </td>
                                    <td><span className={`badge ${getBadgeClass(inv.invoiceStatus)}`}>{inv.invoiceStatus}</span></td>
                                    <td className="action-cell">
                                        <div className="rf-actions">
                                            {inv.invoiceStatus !== 'PAID' && (
                                                <button className="btn-pay" onClick={() => setSelectedInvoice(inv)}>Pay</button>
                                            )}
                                            <button className="btn-print" onClick={() => handlePrint(inv)}>🖨️ Print</button>
                                            <button className="btn-print" onClick={() => setHistoryInvoice(inv)}>🗒 History</button>
                                            {inv.invoiceStatus !== 'CANCELLED' && (
                                                <button className="btn-print" onClick={() => handleExtension(inv)}>+ Extend</button>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                    {invoices.length === 0 && <div className="rf-empty">No invoices yet.</div>}
                </div>
            )}

            {selectedInvoice && (
                <PaymentModal
                    invoice={selectedInvoice}
                    onClose={() => setSelectedInvoice(null)}
                    onSuccess={() => { setSelectedInvoice(null); fetchList(); }}
                />
            )}

            {invoicePreview && (
                <InvoicePreviewModal
                    preview={invoicePreview}
                    onClose={() => setInvoicePreview(null)}
                    onSuccess={() => { setInvoicePreview(null); setRentalIdInput(''); fetchList(); }}
                />
            )}

            {historyInvoice && (
                <PaymentHistoryModal
                    invoice={historyInvoice}
                    onClose={() => setHistoryInvoice(null)}
                />
            )}

            {extensionPreview && (
                <ExtensionUpdateModal
                    preview={extensionPreview}
                    onClose={() => setExtensionPreview(null)}
                    onSuccess={() => { setExtensionPreview(null); fetchList(); }}
                />
            )}
        </div>
    );
}