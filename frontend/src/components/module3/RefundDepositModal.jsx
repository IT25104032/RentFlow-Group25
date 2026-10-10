import React, { useState } from 'react';
import { refundDeposit } from '../../services/module3/depositApi';

export default function RefundDepositModal({ deposit, onClose, onSuccess }) {
    const [deduction, setDeduction] = useState('0');
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');

    const received = Number(deposit.depositAmountReceived) || 0;
    const alreadyDeducted = Number(deposit.amountDeducted) || 0;
    const available = received - alreadyDeducted; // part already used on an invoice is not refundable
    const deductionNum = parseFloat(deduction) || 0;
    const refundAmount = available - deductionNum;

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (deductionNum < 0) return setError('Deduction cannot be negative.');
        if (deductionNum > available) return setError(`Deduction cannot exceed the remaining deposit of Rs. ${available.toFixed(2)}`);

        setSubmitting(true);
        try {
            await refundDeposit(deposit.depositId, deductionNum);
            onSuccess();
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <h3>Refund Deposit for Rental #{deposit.rentalId}</h3>
                <p>Deposit Received: <strong>Rs. {received.toFixed(2)}</strong></p>
                {alreadyDeducted > 0 && (
                    <p>Already Deducted: <strong>Rs. {alreadyDeducted.toFixed(2)}</strong></p>
                )}
                <p>Remaining Deposit: <strong>Rs. {available.toFixed(2)}</strong></p>

                {error && <div className="error-banner">{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Further deduction for damage / loss (Rs.):</label>
                        <input type="number" step="0.01" min="0" value={deduction} onChange={(e) => setDeduction(e.target.value)} />
                    </div>
                    <p>Amount to refund: <strong>Rs. {Math.max(refundAmount, 0).toFixed(2)}</strong></p>
                    <div className="modal-actions">
                        <button type="button" className="btn-cancel" onClick={onClose} disabled={submitting}>Cancel</button>
                        <button type="submit" className="btn-submit" disabled={submitting}>
                            {submitting ? 'Processing...' : 'Confirm Refund'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
