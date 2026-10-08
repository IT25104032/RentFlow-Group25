import { useEffect, useState } from "react";
import { NavLink, Outlet, useLocation } from "react-router-dom";

import { getOverview } from "../../services/module4/module4Api";
import "../../styles/module4/module4.css";

// =========================================================
// MODULE 4 - navigation bar shown on top of every Module 4 page.
// It sits inside the shared AppLayout (left sidebar), so the
// sidebar still works and this bar moves between Module 4 screens.
// =========================================================

const TABS = [
    { to: "/returns", label: "Overview", end: true },
    { to: "/returns/new", label: "Process return", badge: "rentalsOut" },
    { to: "/returns/history", label: "Return history" },
    { to: "/returns/damages", label: "Damage records", badge: "damagesToResolve" },
    { to: "/returns/lost", label: "Lost items", badge: "lostPending" },
    { to: "/returns/settlements", label: "Settlements", badge: "readyToSettle" }
];

function Module4Layout() {
    const location = useLocation();
    const [counts, setCounts] = useState(null);

    // refresh the badge numbers whenever the user moves between pages
    useEffect(() => {
        let alive = true;
        getOverview()
            .then((data) => alive && setCounts(data))
            .catch(() => alive && setCounts(null));
        return () => {
            alive = false;
        };
    }, [location.pathname, location.key]);

    // detail pages keep their parent tab highlighted
    const path = location.pathname;
    const isReturnDetail = /^\/returns\/\d+$/.test(path);

    return (
        <div className="m4-page">
            <header className="m4-topbar">
                <div className="m4-topbar-title">
                    <span className="m4-topbar-icon">↩</span>
                    <div>
                        <h1>Returns &amp; Settlement</h1>
                        <p>Check equipment back in, record damage and losses, and settle the bill.</p>
                    </div>
                </div>

                <nav className="m4-tabs" aria-label="Module 4">
                    {TABS.map((tab) => {
                        const count = tab.badge && counts ? counts[tab.badge] : 0;
                        return (
                            <NavLink
                                key={tab.to}
                                to={tab.to}
                                end={tab.end}
                                className={({ isActive }) =>
                                    "m4-tab" + (isActive || (isReturnDetail && tab.to === "/returns/history") ? " active" : "")
                                }
                            >
                                {tab.label}
                                {count > 0 && <span className="m4-tab-badge">{count}</span>}
                            </NavLink>
                        );
                    })}
                </nav>
            </header>

            <div className="m4-content">
                <Outlet context={{ counts }} />
            </div>
        </div>
    );
}

export default Module4Layout;
