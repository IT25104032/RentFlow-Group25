import React, { useState, useEffect } from 'react';
import { getDeposits } from '../../services/module3/depositApi';
import ReceiveDepositModal from '../../components/module3/ReceiveDepositModal';
import RefundDepositModal from '../../components/module3/RefundDepositModal';
import BillingTabs from '../../components/module3/BillingTabs';
import '../../styles/module3/PaymentManagement.css';

export default function SecurityDeposits() {
    const [deposits, setDeposits] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [receiveTarget, setReceiveTarget] = useState(null);
    const [refundTarget, setRefundTarget] = useState(null);

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

    const getBadgeClass = (status) => {
        if (status === 'REFUNDED') return 'badge-paid';
        if (status === 'HELD' || status === 'PARTIALLY_REFUNDED') return 'badge-partial';
        return 'badge-unpaid'; // PENDING, FORFEITED
    };

    const formatDate = (value) => (value ? value.replace('T', ' ').substring(0, 16) : '-');
    const money = (value) => `Rs. ${Number(value || 0).toFixed(2)}`;

    return (
        <div className="payment-container">
            <div className="rf-page-head">
                <h1>Billing &amp; Payments</h1>
                <BillingTabs />
            </div>

            <div className="rf-section-head">
                <div>
                    <h2>Security Deposits</h2>
                    <p>
                        Deposits appear here automatically when a rental is created. Click Receive when the customer pays it.
                    </p>
                </div>

                <div className="rf-toolbar">
                    <button type="button" className="btn-refresh" onClick={fetchList}>↻ Refresh</button>
                </div>
            </div>

            {error && <div className="error-banner">{error}</div>}

            {!loading && (
                <div className="rf-table-card"><div className="rf-table-scroll">
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
                            <td style={{ color: dep.amountDeducted > 0 ? 'var(--rf-danger)' : 'inherit', fontWeight: dep.amountDeducted > 0 ? 700 : undefined }}>{money(dep.amountDeducted)}</td>
                            <td>{money(dep.amountRefunded)}</td>
                            <td style={{ whiteSpace: 'nowrap' }}>{formatDate(dep.receivedDate)}</td>
                            <td style={{ whiteSpace: 'nowrap' }}>{formatDate(dep.refundDate)}</td>
                            <td><span className={`badge ${getBadgeClass(dep.depositStatus)}`}>{dep.depositStatus}</span></td>
                            <td className="action-cell">
                                {dep.depositStatus === 'PENDING' && (
                                    <button className="btn-pay" onClick={() => setReceiveTarget(dep)}>Receive</button>
                                )}
                                {dep.depositStatus === 'HELD' && (
                                    <button className="btn-pay" onClick={() => setRefundTarget(dep)}>Refund</button>
                                )}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
                </div></div>
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
