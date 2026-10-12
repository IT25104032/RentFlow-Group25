import React, { useState, useEffect } from 'react';
import { getDeposits, createDeposit } from '../../services/module3/depositApi';
import ReceiveDepositModal from '../../components/module3/ReceiveDepositModal';
import RefundDepositModal from '../../components/module3/RefundDepositModal';
import '../../styles/module3/PaymentManagement.css';

export default function SecurityDeposits() {
    const [deposits, setDeposits] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [receiveTarget, setReceiveTarget] = useState(null);
    const [refundTarget, setRefundTarget] = useState(null);

    // State for creating a new deposit
    const [rentalIdInput, setRentalIdInput] = useState('');
    const [amountInput, setAmountInput] = useState('');
    const [creating, setCreating] = useState(false);

    const fetchList = async () => {
        setLoading(true);
        try {
            const data = await getDeposits();
            setDeposits(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchList(); }, []);

    const handleCreate = async (e) => {
        e.preventDefault();
        setCreating(true);
        setError(null);
        try {
            await createDeposit(Number(rentalIdInput), Number(amountInput));
            setRentalIdInput('');
            setAmountInput('');
            await fetchList();
        } catch (err) {
            setError(err.message);
        } finally {
            setCreating(false);
        }
    };

    const getBadgeClass = (status) => {
        if (status === 'REFUNDED') return 'badge-paid';
        if (status === 'HELD' || status === 'PARTIALLY_REFUNDED') return 'badge-partial';
        return 'badge-unpaid'; // PENDING, FORFEITED
    };

    const formatDate = (value) => (value ? value.replace('T', ' ').substring(0, 16) : '-');
    const money = (value) => `Rs. ${Number(value || 0).toFixed(2)}`;

    return (
        <div className="payment-container">
            <div className="header-bar">
                <h2>Security Deposits</h2>

                <form onSubmit={handleCreate} style={{ display: 'flex', gap: '10px' }}>
                    <input
                        type="number"
                        placeholder="Rental ID"
                        value={rentalIdInput}
                        onChange={(e) => setRentalIdInput(e.target.value)}
                        style={{ padding: '8px 12px', borderRadius: '4px', border: '1px solid #ccc', width: '110px' }}
                        required
                    />
                    <input
                        type="number"
                        step="0.01"
                        placeholder="Deposit (Rs.)"
                        value={amountInput}
                        onChange={(e) => setAmountInput(e.target.value)}
                        style={{ padding: '8px 12px', borderRadius: '4px', border: '1px solid #ccc', width: '130px' }}
                        required
                    />
                    <button
                        type="submit"
                        disabled={creating}
                        style={{ backgroundColor: '#28a745', color: '#fff', border: 'none', padding: '8px 16px', borderRadius: '4px', cursor: 'pointer' }}
                    >
                        {creating ? 'Creating...' : '+ New Deposit'}
                    </button>
                    <button type="button" className="btn-refresh" onClick={fetchList}>↻ Refresh</button>
                </form>
            </div>

            {error && <div className="error-banner">{error}</div>}

            {!loading && (
                <table className="invoice-table">
                    <thead>
                    <tr>
                        <th>Deposit #</th>
                        <th>Rental ID</th>
                        <th>Required</th>
                        <th>Received</th>
                        <th>Deducted</th>
                        <th>Refunded</th>
                        <th>Received On</th>
                        <th>Refunded On</th>
                        <th>Status</th>
                        <th className="action-header">Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    {deposits.map((dep) => (
                        <tr key={dep.depositId}>
                            <td>#{dep.depositId}</td>
                            <td>{dep.rentalId}</td>
                            <td>{money(dep.calculatedDeposit)}</td>
                            <td>{money(dep.depositAmountReceived)}</td>
                            <td style={{ color: dep.amountDeducted > 0 ? '#d9534f' : 'inherit' }}>{money(dep.amountDeducted)}</td>
                            <td>{money(dep.amountRefunded)}</td>
                            <td style={{ whiteSpace: 'nowrap' }}>{formatDate(dep.receivedDate)}</td>
                            <td style={{ whiteSpace: 'nowrap' }}>{formatDate(dep.refundDate)}</td>
                            <td><span className={`badge ${getBadgeClass(dep.depositStatus)}`}>{dep.depositStatus}</span></td>
                            <td className="action-cell">
                                {dep.depositStatus === 'PENDING' && (
                                    <button className="btn-pay" onClick={() => setReceiveTarget(dep)}>Receive</button>
                                )}
                                {dep.depositStatus === 'HELD' && (
                                    <button className="btn-pay" style={{ backgroundColor: '#17a2b8' }} onClick={() => setRefundTarget(dep)}>Refund</button>
                                )}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}

            {receiveTarget && (
                <ReceiveDepositModal
                    deposit={receiveTarget}
                    onClose={() => setReceiveTarget(null)}
                    onSuccess={() => { setReceiveTarget(null); fetchList(); }}
                />
            )}

            {refundTarget && (
                <RefundDepositModal
                    deposit={refundTarget}
                    onClose={() => setRefundTarget(null)}
                    onSuccess={() => { setRefundTarget(null); fetchList(); }}
                />
            )}
        </div>
    );
}
