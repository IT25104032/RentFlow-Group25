import React, { useState, useEffect } from 'react';
import { getInvoices, getChargesByInvoice, previewInvoice } from '../../services/module3/paymentApi';
import InvoicePreviewModal from '../../components/module3/InvoicePreviewModal';
import PaymentModal from '../../components/module3/PaymentModal';
import AddChargeModal from '../../components/module3/AddChargeModal';
import '../../styles/module3/PaymentManagement.css';
import PaymentHistoryModal from '../../components/module3/PaymentHistoryModal';

export default function PaymentManagement() {
    const [invoices, setInvoices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedInvoice, setSelectedInvoice] = useState(null);
    const [chargeInvoice, setChargeInvoice] = useState(null);
    const [invoicePreview, setInvoicePreview] = useState(null);
    const [historyInvoice, setHistoryInvoice] = useState(null);
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

    return (
        <div className="payment-container">
            <div className="header-bar" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h2>Billing & Payment Management</h2>

                {/* NEW INVOICE GENERATION BAR INJECTED HERE */}
                <form onSubmit={handleGenerateInvoice} style={{ display: 'flex', gap: '10px' }}>
                    <input
                        type="number"
                        placeholder="Enter Rental ID"
                        value={rentalIdInput}
                        onChange={(e) => setRentalIdInput(e.target.value)}
                        style={{ padding: '8px 12px', borderRadius: '4px', border: '1px solid #ccc' }}
                        required
                    />
                    <button
                        type="submit"
                        disabled={generating}
                        style={{ backgroundColor: '#28a745', color: '#fff', border: 'none', padding: '8px 16px', borderRadius: '4px', cursor: 'pointer' }}
                    >
                        {generating ? 'Generating...' : '+ Generate Invoice'}
                    </button>
                    <button type="button" className="btn-refresh" onClick={fetchList}>↻ Refresh</button>
                </form>
            </div>

            {error && <div className="error-banner" style={{ color: 'red', marginBottom: '15px' }}>{error}</div>}

            {!loading && (
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
                            <td style={{ color: inv.balanceDue > 0 ? '#d9534f' : '#28a745' }}>
                                <strong>Rs. {Number(inv.balanceDue).toFixed(2)}</strong>
                            </td>
                            <td><span className={`badge ${getBadgeClass(inv.invoiceStatus)}`}>{inv.invoiceStatus}</span></td>
                            <td className="action-cell">
                                <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                                    {inv.invoiceStatus !== 'PAID' && (
                                        <button className="btn-pay" onClick={() => setSelectedInvoice(inv)}>Pay</button>
                                    )}
                                    <button className="btn-pay" style={{ backgroundColor: '#17a2b8' }} onClick={() => setChargeInvoice(inv)}>+ Fee</button>
                                    <button className="btn-print" onClick={() => handlePrint(inv)}>🖨️️ Print</button>
                                    <button className="btn-pay" style={{ backgroundColor: '#6f42c1' }} onClick={() => setHistoryInvoice(inv)}>History</button>
                                </div>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}

            {selectedInvoice && (
                <PaymentModal
                    invoice={selectedInvoice}
                    onClose={() => setSelectedInvoice(null)}
                    onSuccess={() => { setSelectedInvoice(null); fetchList(); }}
                />
            )}

            {chargeInvoice && (
                <AddChargeModal
                    invoice={chargeInvoice}
                    onClose={() => setChargeInvoice(null)}
                    onSuccess={() => { setChargeInvoice(null); fetchList(); }}
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
        </div>
    );
}