import React, { useEffect, useState } from 'react';
import { getPaymentHistory } from '../../services/module3/paymentApi';

// Lists every payment recorded against an invoice
export default function PaymentHistoryModal({ invoice, onClose }) {
    const [payments, setPayments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        getPaymentHistory(invoice.invoiceId)
            .then(setPayments)
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    }, [invoice.invoiceId]);

    const money = (value) => `Rs. ${Number(value || 0).toFixed(2)}`;
    const formatDate = (value) => (value ? value.replace('T', ' ').substring(0, 16) : '-');

    const completed = payments.filter((p) => p.paymentStatus === 'COMPLETED');
    const totalPaid = completed.reduce((sum, p) => sum + Number(p.amount), 0);

    const cell = { padding: '6px 8px', borderBottom: '1px solid #eee', fontSize: '0.85rem', textAlign: 'left' };

    return (
        <div className="modal-overlay">
            <div className="modal-content" style={{ width: '640px', maxHeight: '90vh', overflowY: 'auto' }}>
                <h3>Payment History for Invoice #{invoice.invoiceId}</h3>
                <p style={{ fontSize: '0.9rem', color: '#555' }}>
                    Total: {money(invoice.totalAmount)} &nbsp;|&nbsp; Paid: {money(invoice.amountPaid)} &nbsp;|&nbsp; Balance: {money(invoice.balanceDue)}
                </p>

                {error && <div className="error-banner">{error}</div>}

                {loading ? (
                    <p>Loading...</p>
                ) : payments.length === 0 ? (
                    <p>No payments have been recorded for this invoice yet.</p>
                ) : (
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                        <tr>
                            <th style={cell}>Date</th>
                            <th style={cell}>Amount</th>
                            <th style={cell}>Method</th>
                            <th style={cell}>Reference</th>
                            <th style={cell}>Received By</th>
                            <th style={cell}>Status</th>
                        </tr>
                        </thead>
                        <tbody>
                        {payments.map((p) => (
                            <tr key={p.paymentId} style={{ opacity: p.paymentStatus === 'VOIDED' ? 0.5 : 1 }}>
                                <td style={{ ...cell, whiteSpace: 'nowrap' }}>{formatDate(p.paymentDate)}</td>
                                <td style={{ ...cell, whiteSpace: 'nowrap' }}>{money(p.amount)}</td>
                                <td style={cell}>{p.paymentMethod.replace('_', ' ')}</td>
                                <td style={cell}>{p.referenceNo || '-'}</td>
                                <td style={cell}>{p.receivedBy ? `Staff #${p.receivedBy}` : '-'}</td>
                                <td style={cell}>{p.paymentStatus}</td>
                            </tr>
                        ))}
                        <tr>
                            <td style={{ ...cell, fontWeight: 'bold' }}>Total Paid</td>
                            <td style={{ ...cell, fontWeight: 'bold' }} colSpan={5}>{money(totalPaid)}</td>
                        </tr>
                        </tbody>
                    </table>
                )}

                <div className="modal-actions">
                    <button type="button" className="btn-cancel" onClick={onClose}>Close</button>
                </div>
            </div>
        </div>
    );
}
