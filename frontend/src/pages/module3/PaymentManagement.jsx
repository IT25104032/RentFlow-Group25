import React, { useState, useEffect } from 'react';
import { getInvoices } from '../../services/module3/paymentApi';
import PaymentModal from '../../components/module3/PaymentModal';
import AddChargeModal from '../../components/module3/AddChargeModal';
import '../../styles/module3/PaymentManagement.css';

export default function PaymentManagement() {
    const [invoices, setInvoices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedInvoice, setSelectedInvoice] = useState(null);
    const [chargeInvoice, setChargeInvoice] = useState(null);

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

    const getBadgeClass = (status) => {
        if (status === 'PAID') return 'badge-paid';
        if (status === 'PARTIALLY_PAID') return 'badge-partial';
        return 'badge-unpaid';
    };

    const handlePrint = (inv) => {
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
                    .status { font-weight: bold; font-size: 18px; margin-top: 20px; text-align: right; }
                </style>
            </head>
            <body>
                <div class="header">RentFlow</div>
                <div class="sub-header">Official Invoice Receipt</div>
                
                <div class="details">
                    <strong>Invoice #:</strong> ${inv.invoiceId}<br>
                    <strong>Rental ID:</strong> ${inv.rentalId}<br>
                    <strong>Date:</strong> ${inv.invoiceDate}<br>
                    <strong>Due Date:</strong> ${inv.dueDate}
                </div>

                <table>
                    <tr><th>Description</th><th>Amount</th></tr>
                    <tr><td>Subtotal</td><td>Rs. ${Number(inv.subtotal).toFixed(2)}</td></tr>
                    <tr><td>Additional Charges</td><td>Rs. ${Number(inv.additionalCharges || 0).toFixed(2)}</td></tr>
                    <tr><td><strong>Total Amount</strong></td><td><strong>Rs. ${Number(inv.totalAmount).toFixed(2)}</strong></td></tr>
                    <tr><td>Amount Paid</td><td>Rs. ${Number(inv.amountPaid).toFixed(2)}</td></tr>
                    <tr><td><strong>Balance Due</strong></td><td><strong>Rs. ${Number(inv.balanceDue).toFixed(2)}</strong></td></tr>
                </table>
                
                <div class="status">Status: ${inv.invoiceStatus}</div>
                
                <script>
                    window.print();
                    setTimeout(() => window.close(), 500);
                </script>
            </body>
            </html>
        `);
        printWindow.document.close();
    };

    return (
        <div className="payment-container">
            <div className="header-bar">
                <h2>Billing & Payment Management</h2>
                <button className="btn-refresh" onClick={fetchList}>↻ Refresh</button>
            </div>

            {error && <div className="error-banner">{error}</div>}

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
                                    {inv.invoiceStatus !== 'PAID' ? (
                                        <button className="btn-pay" onClick={() => setSelectedInvoice(inv)}>Pay</button>
                                    ) : <span style={{ color: '#888', fontSize: '13px' }}>Paid</span>}
                                    <button className="btn-pay" style={{ backgroundColor: '#17a2b8' }} onClick={() => setChargeInvoice(inv)}>+ Fee</button>
                                    <button className="btn-print" onClick={() => handlePrint(inv)}>🖨️ Print</button>
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
        </div>
    );
}