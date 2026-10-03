import React, { useState } from 'react';
import { addCharge, recalculateInvoice } from '../../services/module3/paymentApi';

export default function AddChargeModal({ invoice, onClose, onSuccess }) {
    const [chargeType, setChargeType] = useState('LATE');
    const [description, setDescription] = useState('');
    const [amount, setAmount] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!amount || Number(amount) <= 0) {
            setError('Please enter a valid charge amount');
            return;
        }

        setLoading(true);
        setError(null);

        try {
            //Post charge entity matching Charge.java
            const chargePayload = {
                rentalId: invoice.rentalId,
                invoice: { invoiceId: invoice.invoiceId },
                chargeType: chargeType,
                chargeDescription: description,
                amount: parseFloat(amount),
                chargeDate: new Date().toISOString(),
                createdBy: 1 // Default system user ID until Auth module integration
            };

            await addCharge(chargePayload);

            //Recalculate totals & balances on backend
            await recalculateInvoice(invoice.invoiceId);

            onSuccess();
        } catch (err) {
            setError(err.message || 'Failed to process charge');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <h3>Add Extra Charge — Invoice #{invoice.invoiceId}</h3>
                {error && <div className="error-banner">{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Charge Type:</label>
                        <select
                            value={chargeType}
                            onChange={(e) => setChargeType(e.target.value)}
                        >
                            <option value="LATE">Late Fee</option>
                            <option value="DAMAGE">Damage Fee</option>
                            <option value="EXTENSION">Extension Fee</option>
                            <option value="LOST_ITEM">Lost Item Fee</option>
                            <option value="OTHER">Other Fee</option>
                        </select>
                    </div>

                    <div className="form-group">
                        <label>Description:</label>
                        <input
                            type="text"
                            placeholder="e.g. Scratched lens casing / 2 days overdue"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Amount (Rs.):</label>
                        <input
                            type="number"
                            step="0.01"
                            placeholder="0.00"
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            required
                        />
                    </div>

                    <div className="modal-actions" style={{ marginTop: '20px', display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                        <button type="button" className="btn-cancel" onClick={onClose} disabled={loading}>
                            Cancel
                        </button>
                        <button type="submit" className="btn-submit" disabled={loading}>
                            {loading ? 'Processing...' : 'Add Fee & Recalculate'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}