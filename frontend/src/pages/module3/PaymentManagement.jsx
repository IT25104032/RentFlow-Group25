import React, { useState, useEffect } from 'react';
import { getInvoices } from '../../services/module3/paymentApi';
import PaymentModal from '../../components/module3/PaymentModal';
import '../../styles/module3/PaymentManagement.css';

export default function PaymentManagement() {
    const [invoices, setInvoices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedInvoice, setSelectedInvoice] = useState(null);

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
                        <th>Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    {invoices.map((inv) => (
                        <tr key={inv.invoiceId}>
                            <td>#{inv.invoiceId}</td>
                            <td>{inv.rentalId}</td>
                            <td>{inv.invoiceDate}</td>
                            <td>{inv.dueDate}</td>
                            <td>${Number(inv.subtotal).toFixed(2)}</td>
                            <td>${Number(inv.totalAmount).toFixed(2)}</td>
                            <td>${Number(inv.amountPaid).toFixed(2)}</td>
                            <td style={{ color: inv.balanceDue > 0 ? '#d9534f' : '#28a745' }}>
                                <strong>${Number(inv.balanceDue).toFixed(2)}</strong>
                            </td>
                            <td><span className={`badge ${getBadgeClass(inv.invoiceStatus)}`}>{inv.invoiceStatus}</span></td>
                            <td>
                                {inv.invoiceStatus !== 'PAID' ? (
                                    <button className="btn-pay" onClick={() => setSelectedInvoice(inv)}>Record Payment</button>
                                ) : <span style={{ color: '#888' }}>Paid in Full</span>}
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
        </div>
    );
}