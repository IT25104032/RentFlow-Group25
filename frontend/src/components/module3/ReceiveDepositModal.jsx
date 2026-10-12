import React, { useState } from 'react';
import { receiveDeposit } from '../../services/module3/depositApi';

export default function ReceiveDepositModal({ deposit, onClose, onSuccess }) {
    const [amount, setAmount] = useState(deposit.calculatedDeposit || '');
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        const amountNum = parseFloat(amount);

        if (isNaN(amountNum) || amountNum <= 0) return setError('Enter a valid amount.');

        setSubmitting(true);
        try {
            await receiveDeposit(deposit.depositId, amountNum);
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
                <h3>Receive Deposit for Rental #{deposit.rentalId}</h3>
                <p>Required Deposit: <strong>Rs. {Number(deposit.calculatedDeposit).toFixed(2)}</strong></p>

                {error && <div className="error-banner">{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Amount Received (Rs.):</label>
                        <input type="number" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} required />
                    </div>
                    <div className="modal-actions">
                        <button type="button" className="btn-cancel" onClick={onClose} disabled={submitting}>Cancel</button>
                        <button type="submit" className="btn-submit" disabled={submitting}>
                            {submitting ? 'Saving...' : 'Confirm'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
