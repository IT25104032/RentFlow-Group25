import { useEffect, useState } from "react";
import {
    NavLink,
    Outlet,
    useLocation
} from "react-router-dom";

import { getOverview } from "../../services/module4/module4Api";
import "../../styles/module4/module4.css";

const tabs = [
    {
        label: "Overview",
        path: "/returns",
        icon: "▦",
        end: true
    },
    {
        label: "Process Return",
        path: "/returns/new",
        icon: "↩"
    },
    {
        label: "Return History",
        path: "/returns/history",
        icon: "◷"
    },
    {
        label: "Damage Records",
        path: "/returns/damages",
        icon: "⚠"
    },
    {
        label: "Lost Items",
        path: "/returns/lost",
        icon: "⊘"
    },
    {
        label: "Settlements",
        path: "/returns/settlements",
        icon: "▤"
    }
];

function Module4Layout() {

    const [counts, setCounts] = useState({});
    const location = useLocation();

    useEffect(() => {
        getOverview()
            .then((data) => {
                setCounts(data);
            })
            .catch((error) => {
                console.error(
                    "Failed to load Module 4 overview:",
                    error
                );
            });
    }, []);

    return (
        <div className="m4-page">

            {/* MODULE 4 HEADER AND NAVIGATION */}
            <header className="m4-topbar">

                <div className="m4-topbar-title">

                    <div className="m4-topbar-icon">
                        ↩
                    </div>

                    <div>
                        <h1>Returns & Settlement</h1>
                        <p>
                            Manage equipment returns, damage,
                            lost items and final settlements
                        </p>
                    </div>

                </div>

                <nav className="m4-tabs" aria-label="Returns navigation">

                    {tabs.map((tab) => {

                        const isActive = tab.end
                            ? location.pathname === tab.path
                            : location.pathname === tab.path ||
                            location.pathname.startsWith(tab.path + "/");

                        return (
                            <NavLink
                                key={tab.path}
                                to={tab.path}
                                end={tab.end}
                                className={
                                    `m4-tab ${isActive ? "active" : ""}`
                                }
                            >
                                <span aria-hidden="true">
                                    {tab.icon}
                                </span>

                                <span>{tab.label}</span>

                            </NavLink>
                        );
                    })}

                </nav>

            </header>

            {/* MODULE 4 PAGE CONTENT */}
            <main className="m4-content">

                <Outlet context={{ counts }} />

            </main>

        </div>
    );
}

export default Module4Layout;