import React, { useState } from 'react';
import { generateInvoice } from '../../services/module3/paymentApi';

// Shows the invoice breakdown for review before it is saved
export default function InvoicePreviewModal({ preview, onClose, onSuccess }) {
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');

    const money = (value) => `Rs. ${Number(value || 0).toFixed(2)}`;

    const handleConfirm = async () => {
        setSubmitting(true);
        setError('');
        try {
            await generateInvoice(preview.rentalId);
            onSuccess();
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const cell = { padding: '6px 8px', borderBottom: '1px solid #eee', fontSize: '0.85rem' };
    const right = { ...cell, textAlign: 'right', whiteSpace: 'nowrap' };

    return (
        <div className="modal-overlay">
            <div className="modal-content" style={{ width: '620px', maxHeight: '90vh', overflowY: 'auto' }}>
                <h3>Invoice Preview for Rental #{preview.rentalId}</h3>
                <p style={{ fontSize: '0.9rem', color: '#555' }}>
                    Rental period: {preview.startDate} to {preview.dueDate} ({preview.rentalDays} days)
                </p>

                {error && <div className="error-banner">{error}</div>}

                <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '15px' }}>
                    <thead>
                    <tr>
                        <th style={cell}>Item</th>
                        <th style={right}>Qty</th>
                        <th style={right}>Rate</th>
                        <th style={right}>Period</th>
                        <th style={right}>Amount</th>
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
                    {preview.additionalLines.map((line) => (
                        <tr key={`c${line.chargeId}`}>
                            <td style={cell} colSpan={4}>{line.chargeType}{line.description ? ` - ${line.description}` : ''}</td>
                            <td style={right}>{money(line.amount)}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>

                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <tbody>
                    <tr><td style={cell}>Rental Charges</td><td style={right}>{money(preview.rentalCharges)}</td></tr>
                    <tr><td style={cell}>Additional Charges</td><td style={right}>{money(preview.additionalCharges)}</td></tr>
                    <tr>
                        <td style={cell}>Security Deposit Deduction</td>
                        <td style={{ ...right, color: '#28a745' }}>- {money(preview.depositDeduction)}</td>
                    </tr>
                    <tr>
                        <td style={{ ...cell, fontWeight: 'bold' }}>Total Payable</td>
                        <td style={{ ...right, fontWeight: 'bold' }}>{money(preview.totalPayable)}</td>
                    </tr>
                    </tbody>
                </table>

                <div className="modal-actions">
                    <button type="button" className="btn-cancel" onClick={onClose} disabled={submitting}>Cancel</button>
                    <button type="button" className="btn-submit" onClick={handleConfirm} disabled={submitting}>
                        {submitting ? 'Generating...' : 'Confirm & Generate'}
                    </button>
                </div>
            </div>
        </div>
    );
}
