import React, { useState } from 'react';
import { applyExtension } from '../../services/module3/paymentApi';

// Shows previous vs updated amounts after a rental extension, then saves on confirm
export default function ExtensionUpdateModal({ preview, onClose, onSuccess }) {
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');

    const money = (value) => `Rs. ${Number(value || 0).toFixed(2)}`;

    const handleConfirm = async () => {
        setSubmitting(true);
        setError('');
        try {
            await applyExtension(preview.invoiceId);
            onSuccess();
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const cell = { padding: '6px 8px', borderBottom: '1px solid #eee', fontSize: '0.85rem' };
    const right = { ...cell, textAlign: 'right', whiteSpace: 'nowrap' };

    const row = (label, before, after, bold) => (
        <tr>
            <td style={{ ...cell, fontWeight: bold ? 'bold' : 'normal' }}>{label}</td>
            <td style={{ ...right, color: '#888' }}>{before}</td>
            <td style={{ ...right, fontWeight: bold ? 'bold' : 'normal' }}>{after}</td>
        </tr>
    );

    return (
        <div className="modal-overlay">
            <div className="modal-content" style={{ width: '620px', maxHeight: '90vh', overflowY: 'auto' }}>
                <h3>Update Charges for Invoice #{preview.invoiceId}</h3>
                <p style={{ fontSize: '0.9rem', color: '#555' }}>
                    Revised rental period: {preview.startDate} to {preview.dueDate} ({preview.rentalDays} days)
                </p>

                {error && <div className="error-banner">{error}</div>}

                <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '15px' }}>
                    <thead>
                    <tr>
                        <th style={cell}>Item</th>
                        <th style={right}>Qty</th>
                        <th style={right}>Rate</th>
                        <th style={right}>Period</th>
                        <th style={right}>New Amount</th>
                    </tr>
                    </thead>
                    <tbody>
                    {preview.rentalLines.map((line) => (
                        <tr key={line.rentalItemId}>
                            <td style={cell}>{line.itemName}</td>
                            <td style={right}>{line.quantity}</td>
                            <td style={right}>{money(line.ratePerUnit)} / {line.ratePeriod.toLowerCase()}</td>
                            <td style={right}>{line.units}</td>
                            <td style={right}>{money(line.amount)}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>

                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                    <tr>
                        <th style={cell}></th>
                        <th style={right}>Before</th>
                        <th style={right}>After</th>
                    </tr>
                    </thead>
                    <tbody>
                    {row('Rental Charges', money(preview.previousRentalCharges), money(preview.newRentalCharges))}
                    {row('Additional Charges', money(preview.additionalCharges), money(preview.additionalCharges))}
                    {row('Security Deposit Deduction', `- ${money(preview.depositDeduction)}`, `- ${money(preview.depositDeduction)}`)}
                    {row('Total Payable', money(preview.previousTotal), money(preview.newTotal), true)}
                    {row('Amount Paid', money(preview.amountPaid), money(preview.amountPaid))}
                    {row('Balance Due', money(preview.previousBalance), money(preview.newBalance), true)}
                    </tbody>
                </table>

                <div className="modal-actions">
                    <button type="button" className="btn-cancel" onClick={onClose} disabled={submitting}>Cancel</button>
                    <button type="button" className="btn-submit" onClick={handleConfirm} disabled={submitting}>
                        {submitting ? 'Updating...' : 'Confirm Update'}
                    </button>
                </div>
            </div>
        </div>
    );
}
