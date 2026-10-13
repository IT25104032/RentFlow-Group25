import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { useEffect, useRef, useState } from "react";

import {
    getOverdueRentals,
    getRentalsDueSoon
} from "../services/module2/notificationService";

import "./Module2Layout.css";

const COMPANY_ID = 1000;

function AppLayout({ user, onLogout }) {
    const navigate = useNavigate();
    const location = useLocation();

    // Normalize the logged-in user's role
    const role = (user?.role || "")
        .replace("ROLE_", "")
        .toUpperCase();

    const isCompulinAdmin = role === "COMPULIN_ADMIN";
    const isCompanyAdmin = role === "COMPANY_ADMIN";
    const isRentalOfficer = role === "RENTAL_OFFICER";
    const canViewRentalNotifications = isCompanyAdmin || isRentalOfficer;

    // Sidebar active page
    const isCreateRental = [
        "/",
        "/renters",
        "/rentals/create",
        "/rentals/equipment",
        "/rentals/review"
    ].includes(location.pathname);

    const isRentalHistory = location.pathname === "/rental-history";
    const isIssueEquipment = location.pathname === "/issue-equipment";
    const isRentalExtension = location.pathname === "/rental-extension";

    // Notification state
    const [notificationOpen, setNotificationOpen] = useState(false);
    const [overdueRentals, setOverdueRentals] = useState([]);
    const [dueSoonRentals, setDueSoonRentals] = useState([]);
    const [notificationLoading, setNotificationLoading] = useState(true);

    const notificationRef = useRef(null);

    const notificationCount =
        overdueRentals.length + dueSoonRentals.length;

    // Load rental notifications
    const loadNotifications = async () => {
        try {
            setNotificationLoading(true);

            const [overdue, dueSoon] = await Promise.all([
                getOverdueRentals(COMPANY_ID),
                getRentalsDueSoon(COMPANY_ID)
            ]);

            setOverdueRentals(Array.isArray(overdue) ? overdue : []);
            setDueSoonRentals(Array.isArray(dueSoon) ? dueSoon : []);

        } catch (error) {
            console.error(
                "Failed to load rental notifications:",
                error
            );
        } finally {
            setNotificationLoading(false);
        }
    };

    // Load notifications and refresh every 60 seconds
    useEffect(() => {
        if (!canViewRentalNotifications) {
            setOverdueRentals([]);
            setDueSoonRentals([]);
            setNotificationOpen(false);
            return;
        }
        loadNotifications();

        const interval = setInterval(() => {
            loadNotifications();
        }, 60000);

        return () => clearInterval(interval);
    }, [canViewRentalNotifications]);

    // Close notification dropdown when clicking outside
    useEffect(() => {
        const handleOutsideClick = (event) => {
            if (
                notificationRef.current &&
                !notificationRef.current.contains(event.target)
            ) {
                setNotificationOpen(false);
            }
        };

        if (notificationOpen) {
            document.addEventListener(
                "mousedown",
                handleOutsideClick
            );
        }

        return () => {
            document.removeEventListener(
                "mousedown",
                handleOutsideClick
            );
        };
    }, [notificationOpen]);

    // Format rental due dates
    const formatDate = (date) => {
        if (!date) {
            return "No due date";
        }

        return new Date(
            `${date}T00:00:00`
        ).toLocaleDateString("en-GB", {
            day: "2-digit",
            month: "short",
            year: "numeric"
        });
    };

    // Toggle notification dropdown and refresh
    const handleNotificationClick = () => {
        setNotificationOpen(previous => !previous);
        loadNotifications();
    };

    // Open selected rental from a notification
    const handleNotificationItemClick = (rentalId, customerId) => {
        setNotificationOpen(false);

        navigate("/rental-history", {
            state: {
                rentalId,
                customerId
            }
        });
    };

    // Reusable sidebar navigation item
    const renderNavigationItem = (label, icon, path, active) => (
        <div
            className={`module2-sidebar-item ${active ? "active" : ""}`}
            onClick={() => navigate(path)}
            key={path}
        >
            <span className="module2-sidebar-icon">
                {icon}
            </span>
            <span>{label}</span>
        </div>
    );

    // Reusable rental notification item
    const renderNotificationItem = (rental, type) => (
        <div
            className={`module2-notification-item ${type}`}
            key={`${type}-${rental.rentalId}`}
            role="button"
            tabIndex={0}
            onClick={() =>
                handleNotificationItemClick(
                    rental.rentalId,
                    rental.customerId
                )
            }
            onKeyDown={(event) => {
                if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();

                    handleNotificationItemClick(
                        rental.rentalId,
                        rental.customerId
                    );
                }
            }}
        >
            <div className={`module2-notification-item-icon ${type}`}>
                {type === "overdue" ? "!" : "⏰"}
            </div>

            <div className="module2-notification-item-content">
                <strong>
                    Rental #{rental.rentalId}
                </strong>

                <span>
                    Customer #{rental.customerId}
                </span>

                <small>
                    Due: {formatDate(rental.dueDate)}
                </small>
            </div>
        </div>
    );

    return (
        <div className="module2-layout">

            {/* SHARED SIDEBAR */}
            <aside className="module2-sidebar">

                <div className="module2-sidebar-brand">
                    <div className="module2-sidebar-logo">
                        RentFlow
                    </div>
                </div>

                <div className="module2-sidebar-divider" />


                <nav className="module2-sidebar-navigation">

                    {/* MODULE 1 — COMPANY AND INVENTORY */}
                    {(isCompulinAdmin || isCompanyAdmin || isRentalOfficer) && (
                        <>
                            <div className="module2-sidebar-section-title">COMPANY & INVENTORY</div>
                            {renderNavigationItem(
                                "Dashboard", "📊", "/dashboard", location.pathname === "/dashboard"
                            )}
                            {isCompulinAdmin && renderNavigationItem(
                                "Companies", "🏢", "/companies", location.pathname === "/companies"
                            )}
                            {isCompanyAdmin && renderNavigationItem(
                                "Company Profile", "🏢", "/company-profile", location.pathname === "/company-profile"
                            )}
                            {isCompanyAdmin && renderNavigationItem(
                                "Users", "👥", "/users", location.pathname === "/users"
                            )}
                            {(isCompanyAdmin || isRentalOfficer) && renderNavigationItem(
                                "Equipment Categories", "📁", "/equipment-categories", location.pathname === "/equipment-categories"
                            )}
                            {(isCompanyAdmin || isRentalOfficer) && renderNavigationItem(
                                "Equipment", "🔧", "/equipment", location.pathname === "/equipment"
                            )}
                        </>
                    )}

                    {/* MODULE 2 — RENTAL MANAGEMENT */}
                    {(isRentalOfficer || isCompanyAdmin) && (
                        <>
                            <div className="module2-sidebar-section-title">
                                RENTAL MANAGEMENT
                            </div>

                            {isRentalOfficer && renderNavigationItem(
                                "Create Rental",
                                "📋",
                                "/renters",
                                isCreateRental
                            )}

                            {renderNavigationItem(
                                "Rental History",
                                "📄",
                                "/rental-history",
                                isRentalHistory
                            )}
                        </>
                    )}

                    {/* MODULE 2 — RENTAL OPERATIONS */}
                    {isRentalOfficer && (
                        <>
                            <div className="module2-sidebar-section-title operations">
                                RENTAL OPERATIONS
                            </div>

                            {renderNavigationItem(
                                "Issue Equipment",
                                "📦",
                                "/issue-equipment",
                                isIssueEquipment
                            )}

                            {renderNavigationItem(
                                "Extend Rental",
                                "📅",
                                "/rental-extension",
                                isRentalExtension
                            )}
                        </>
                    )}

                    {/* MODULE 3 — BILLING & PAYMENTS */}
                    {(isRentalOfficer || isCompanyAdmin) && (
                        <>
                            <div className="module2-sidebar-section-title operations">
                                BILLING & PAYMENTS
                            </div>

                            {isRentalOfficer && (
                                <>
                                    {renderNavigationItem(
                                        "Payment Management",
                                        "💳",
                                        "/payments",
                                        location.pathname === "/payments"
                                    )}

                                    {renderNavigationItem(
                                        "Security Deposits",
                                        "💰",
                                        "/security-deposits",
                                        location.pathname === "/security-deposits"
                                    )}
                                </>
                            )}

                            {renderNavigationItem(
                                "Financial Reports",
                                "📊",
                                "/financial-reports",
                                location.pathname === "/financial-reports"
                            )}
                        </>
                    )}

                    {/* MODULE 4 — RETURNS */}
                    {(isRentalOfficer || isCompanyAdmin) && (
                        <>
                            <div className="module2-sidebar-section-title operations">
                                RETURNS & SETTLEMENTS
                            </div>

                            {isRentalOfficer && renderNavigationItem(
                                "Process Returns",
                                "↩️",
                                "/returns",
                                location.pathname === "/returns" ||
                                location.pathname === "/returns/new"
                            )}

                            {renderNavigationItem(
                                "Return History",
                                "📜",
                                "/returns/history",
                                location.pathname === "/returns/history"
                            )}
                        </>
                    )}



                </nav>


                {/* AUTHENTICATED USER */}
                <div
                    className="module2-sidebar-user"
                    style={{
                        display: "flex",
                        flexDirection: "column",
                        gap: "12px"
                    }}
                >
                    <div style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "10px"
                    }}>
                        <div className="module2-sidebar-user-avatar">
                            {user?.fullName?.charAt(0).toUpperCase() || "U"}
                        </div>

                        <div className="module2-sidebar-user-details">
                            <strong>{user?.fullName || "User"}</strong>
                            <span>
                {isCompulinAdmin
                    ? "Compulin Admin"
                    : isCompanyAdmin
                        ? "Rental Company Admin"
                        : isRentalOfficer
                            ? "Rental Officer"
                            : "Unknown Role"}
            </span>
                        </div>
                    </div>

                    <button
                        type="button"
                        onClick={onLogout}
                        style={{
                            width: "100%",
                            padding: "10px 12px",
                            background: "#28364d",
                            color: "#ffffff",
                            border: "1px solid #45546b",
                            borderRadius: "8px",
                            cursor: "pointer",
                            fontSize: "14px",
                            textAlign: "center"
                        }}
                    >
                        ↪ Logout
                    </button>
                </div>


            </aside>

            {/* SHARED MAIN CONTENT */}
            <main className="module2-layout-content">

                {/* TOP BAR */}
                <div className="module2-topbar">
                    <div className="module2-topbar-spacer" />

                    {/* NOTIFICATION BELL */}
                    {canViewRentalNotifications && (
                        <div
                            className="module2-notification-wrapper"
                            ref={notificationRef}
                        >
                            <button
                                type="button"
                                className="module2-notification-button"
                                onClick={handleNotificationClick}
                                aria-label="Rental notifications"
                                aria-expanded={notificationOpen}
                            >
                            <span className="module2-notification-bell">
                                🔔
                            </span>

                                {notificationCount > 0 && (
                                    <span className="module2-notification-badge">
                                    {notificationCount > 99
                                        ? "99+"
                                        : notificationCount}
                                </span>
                                )}
                            </button>

                            {/* NOTIFICATION DROPDOWN */}
                            {notificationOpen && (
                                <div className="module2-notification-dropdown">

                                    <div className="module2-notification-header">
                                        <div>
                                            <h3>Notifications</h3>
                                            <span>Rental status alerts</span>
                                        </div>

                                        {notificationCount > 0 && (
                                            <span className="module2-notification-total">
                                            {notificationCount}
                                        </span>
                                        )}
                                    </div>

                                    {notificationLoading ? (
                                        <div className="module2-notification-empty">
                                        <span className="module2-notification-loading-icon">
                                            ⟳
                                        </span>

                                            <span>
                                            Checking rental notifications...
                                        </span>
                                        </div>
                                    ) : (
                                        <>
                                            {/* OVERDUE RENTALS */}
                                            <div className="module2-notification-section">

                                                <div className="module2-notification-section-title overdue">
                                                    <span>⚠️</span>
                                                    <span>Overdue Rentals</span>

                                                    <span className="module2-notification-section-count">
                                                    {overdueRentals.length}
                                                </span>
                                                </div>

                                                {overdueRentals.length === 0 ? (
                                                    <div className="module2-notification-no-items">
                                                        No overdue rentals
                                                    </div>
                                                ) : (
                                                    overdueRentals.map(rental =>
                                                        renderNotificationItem(
                                                            rental,
                                                            "overdue"
                                                        )
                                                    )
                                                )}

                                            </div>

                                            {/* DUE SOON RENTALS */}
                                            <div className="module2-notification-section">

                                                <div className="module2-notification-section-title due-soon">
                                                    <span>⏰</span>
                                                    <span>Due Soon</span>

                                                    <span className="module2-notification-section-count">
                                                    {dueSoonRentals.length}
                                                </span>
                                                </div>

                                                {dueSoonRentals.length === 0 ? (
                                                    <div className="module2-notification-no-items">
                                                        No rentals due soon
                                                    </div>
                                                ) : (
                                                    dueSoonRentals.map(rental =>
                                                        renderNotificationItem(
                                                            rental,
                                                            "due-soon"
                                                        )
                                                    )
                                                )}

                                            </div>
                                        </>
                                    )}

                                </div>
                            )}
                        </div>
                    )}
                </div>

                {/* CURRENT ROUTED PAGE */}
                <div className="module2-page-content">
                    <Outlet />
                </div>

            </main>
        </div>
    );
}

export default AppLayout;
