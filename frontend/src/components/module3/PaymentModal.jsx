import React, { useState } from 'react';
import { recordPayment } from '../../services/module3/paymentApi';

export default function PaymentModal({ invoice, onClose, onSuccess }) {
    const [paymentAmount, setPaymentAmount] = useState(invoice.balanceDue || '');
    const [paymentMethod, setPaymentMethod] = useState('CASH');
    const [referenceNo, setReferenceNo] = useState('');
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        const amountNum = parseFloat(paymentAmount);

        if (isNaN(amountNum) || amountNum <= 0) return setError('Enter a valid amount.');
        if (amountNum > invoice.balanceDue) return setError(`Exceeds balance of $${invoice.balanceDue}`);

        setSubmitting(true);
        try {
            await recordPayment({
                invoiceId: invoice.invoiceId,
                amount: amountNum,
                paymentMethod: paymentMethod,
                referenceNo: referenceNo.trim() || `TXN-${Date.now()}`
            });
            onSuccess();
        } catch (err) {
            setError(err.message || 'Failed to record payment.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <h3>Record Payment for Invoice #{invoice.invoiceId}</h3>
                <p>Balance Due: <strong>${Number(invoice.balanceDue).toFixed(2)}</strong></p>

                {error && <div className="error-banner">{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Amount ($):</label>
                        <input type="number" step="0.01" value={paymentAmount} onChange={(e) => setPaymentAmount(e.target.value)} required />
                    </div>
                    <div className="form-group">
                        <label>Payment Method:</label>
                        <select value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value)}>
                            <option value="CASH">CASH</option>
                            <option value="CARD">CARD</option>
                            <option value="BANK_TRANSFER">BANK_TRANSFER</option>
                        </select>
                    </div>
                    <div className="form-group">
                        <label>Reference No:</label>
                        <input type="text" placeholder="Optional" value={referenceNo} onChange={(e) => setReferenceNo(e.target.value)} />
                    </div>
                    <div className="modal-actions">
                        <button type="button" className="btn-cancel" onClick={onClose} disabled={submitting}>Cancel</button>
                        <button type="submit" className="btn-submit" disabled={submitting}>
                            {submitting ? 'Processing...' : 'Submit'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}