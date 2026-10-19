import { NavLink } from 'react-router-dom';

// Visual-only tab strip shared by the three Billing pages (Invoices / Deposits / Reports).
// It only links to existing routes; the company admin role only has access to Reports.
function getRole() {
    try {
        const saved = JSON.parse(sessionStorage.getItem('rentflowUser') || 'null');
        return String(saved?.role || '').replace(/^ROLE_/, '').toUpperCase();
    } catch {
        return '';
    }
}

export default function BillingTabs() {
    if (getRole() === 'COMPANY_ADMIN') return null;

    const tabs = [
        { label: 'Invoices', path: '/payments' },
        { label: 'Deposits', path: '/security-deposits' },
        { label: 'Reports', path: '/financial-reports' }
    ];

    return (
        <nav className="rf-tabs" aria-label="Billing sections">
            {tabs.map((tab) => (
                <NavLink
                    key={tab.path}
                    to={tab.path}
                    className={({ isActive }) => `rf-tab${isActive ? ' active' : ''}`}
                >
                    {tab.label}
                </NavLink>
            ))}
        </nav>
    );
}
