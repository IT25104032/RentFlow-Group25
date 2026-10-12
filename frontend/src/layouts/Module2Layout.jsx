import {
    Outlet,
    useLocation,
    useNavigate
} from "react-router-dom";

import {
    useEffect,
    useRef,
    useState
} from "react";

import {
    getOverdueRentals,
    getRentalsDueSoon
} from "../services/module2/notificationService";

import "./Module2Layout.css";


function Module2Layout() {

    const navigate = useNavigate();
    const location = useLocation();

    // =====================================================
    // SIDEBAR ACTIVE PAGE
    // =====================================================

    const isRentalHistory =
        location.pathname === "/rental-history";

    const isRentalExtension =
        location.pathname === "/rental-extension";

    const isIssueEquipment =
        location.pathname === "/issue-equipment";

    const isCreateRental =
        location.pathname === "/"
        || location.pathname === "/renters"
        || location.pathname === "/rentals/create"
        || location.pathname === "/rentals/equipment"
        || location.pathname === "/rentals/review";


    // =====================================================
    // NOTIFICATION STATE
    // =====================================================

    const COMPANY_ID = 1000;

    const [notificationOpen, setNotificationOpen] = useState(false);

    const [overdueRentals, setOverdueRentals] = useState([]);

    const [dueSoonRentals, setDueSoonRentals] = useState([]);

    const [notificationLoading, setNotificationLoading] = useState(true);

    const notificationRef = useRef(null);


    // =====================================================
    // TOTAL NOTIFICATION COUNT
    // =====================================================

    const notificationCount =
        overdueRentals.length + dueSoonRentals.length;


    // =====================================================
    // LOAD RENTAL NOTIFICATIONS
    // =====================================================

    const loadNotifications = async () => {

        try {

            setNotificationLoading(true);

            const [
                overdue,
                dueSoon
            ] = await Promise.all([

                getOverdueRentals(COMPANY_ID),

                getRentalsDueSoon(COMPANY_ID)

            ]);

            setOverdueRentals(
                Array.isArray(overdue)
                    ? overdue
                    : []
            );

            setDueSoonRentals(
                Array.isArray(dueSoon)
                    ? dueSoon
                    : []
            );

        } catch (error) {

            console.error(
                "Failed to load rental notifications:",
                error
            );

        } finally {

            setNotificationLoading(false);

        }
    };


    // =====================================================
    // LOAD NOTIFICATIONS WHEN LAYOUT OPENS
    // AND REFRESH EVERY 60 SECONDS
    // =====================================================

    useEffect(() => {

        loadNotifications();

        const interval = setInterval(() => {

            loadNotifications();

        }, 60000);

        return () => {

            clearInterval(interval);

        };

    }, []);


    // =====================================================
    // CLOSE NOTIFICATION DROPDOWN
    // WHEN USER CLICKS OUTSIDE
    // =====================================================

    useEffect(() => {

        const handleOutsideClick = (event) => {

            if (
                notificationRef.current
                && !notificationRef.current.contains(event.target)
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


    // =====================================================
    // FORMAT DATE
    // =====================================================

    const formatDate = (date) => {

        if (!date) {
            return "No due date";
        }

        return new Date(
            `${date}T00:00:00`
        ).toLocaleDateString(
            "en-GB",
            {
                day: "2-digit",
                month: "short",
                year: "numeric"
            }
        );

    };


    // =====================================================
    // OPEN NOTIFICATION DROPDOWN
    // ALSO REFRESH DATA
    // =====================================================

    const handleNotificationClick = () => {

        setNotificationOpen(
            previous => !previous
        );

        loadNotifications();

    };

    const handleNotificationItemClick = (
        rentalId,
        customerId
    ) => {

        setNotificationOpen(false);

        navigate("/rental-history", {
            state: {
                rentalId: rentalId,
                customerId: customerId
            }
        });

    };

    return (

        <div className="module2-layout">


            {/* =====================================
                MODULE 2 SIDEBAR
            ===================================== */}

            <aside className="module2-sidebar">


                <div className="module2-sidebar-brand">

                    <div className="module2-sidebar-logo">
                        RentFlow
                    </div>

                </div>


                <div className="module2-sidebar-divider"></div>


                <nav className="module2-sidebar-navigation">


                    <div className="module2-sidebar-section-title">
                        RENTAL MANAGEMENT
                    </div>


                    {/* CREATE RENTAL */}

                    <div
                        className={
                            `module2-sidebar-item ${
                                isCreateRental ? "active" : ""
                            }`
                        }

                        onClick={() =>
                            navigate("/renters")
                        }
                    >

                        <span className="module2-sidebar-icon">
                            📋
                        </span>

                        <span>
                            Create Rental
                        </span>

                    </div>


                    {/* RENTAL HISTORY */}

                    <div
                        className={
                            `module2-sidebar-item ${
                                isRentalHistory ? "active" : ""
                            }`
                        }

                        onClick={() =>
                            navigate("/rental-history")
                        }
                    >

                        <span className="module2-sidebar-icon">
                            📄
                        </span>

                        <span>
                            Rental History
                        </span>

                    </div>


                    <div className="module2-sidebar-section-title operations">
                        RENTAL OPERATIONS
                    </div>


                    {/* ISSUE EQUIPMENT */}

                    <div
                        className={
                            `module2-sidebar-item ${
                                isIssueEquipment ? "active" : ""
                            }`
                        }

                        onClick={() =>
                            navigate("/issue-equipment")
                        }
                    >

                        <span className="module2-sidebar-icon">
                            📦
                        </span>

                        <span>
                            Issue Equipment
                        </span>

                    </div>


                    {/* EXTEND RENTAL */}

                    <div
                        className={
                            `module2-sidebar-item ${
                                isRentalExtension ? "active" : ""
                            }`
                        }

                        onClick={() =>
                            navigate("/rental-extension")
                        }
                    >

                        <span className="module2-sidebar-icon">
                            📅
                        </span>

                        <span>
                            Extend Rental
                        </span>

                    </div>


                </nav>


                <div className="module2-sidebar-user">

                    <div className="module2-sidebar-user-avatar">
                        R
                    </div>

                    <div className="module2-sidebar-user-details">

                        <strong>
                            Rental Officer
                        </strong>

                        <span>
                            Staff User
                        </span>

                    </div>

                </div>


            </aside>


            {/* =====================================
                MAIN CONTENT AREA
            ===================================== */}

            <main className="module2-layout-content">


                {/* =====================================
                    TOP BAR
                ===================================== */}

                <div className="module2-topbar">


                    <div className="module2-topbar-spacer">
                    </div>


                    {/* =====================================
                        NOTIFICATION BUTTON
                    ===================================== */}

                    <div
                        className="module2-notification-wrapper"
                        ref={notificationRef}
                    >

                        <button
                            type="button"
                            className="module2-notification-button"

                            onClick={
                                handleNotificationClick
                            }

                            aria-label="Rental notifications"

                            aria-expanded={
                                notificationOpen
                            }
                        >

                            <span className="module2-notification-bell">
                                🔔
                            </span>


                            {/* NOTIFICATION BADGE */}

                            {notificationCount > 0 && (

                                <span className="module2-notification-badge">

                                    {
                                        notificationCount > 99
                                            ? "99+"
                                            : notificationCount
                                    }

                                </span>

                            )}

                        </button>


                        {/* =====================================
                            NOTIFICATION DROPDOWN
                        ===================================== */}

                        {notificationOpen && (

                            <div className="module2-notification-dropdown">


                                {/* HEADER */}

                                <div className="module2-notification-header">

                                    <div>

                                        <h3>
                                            Notifications
                                        </h3>

                                        <span>
                                            Rental status alerts
                                        </span>

                                    </div>


                                    {notificationCount > 0 && (

                                        <span className="module2-notification-total">

                                            {notificationCount}

                                        </span>

                                    )}

                                </div>


                                {/* =================================
                                    LOADING
                                ================================= */}

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


                                        {/* =================================
                                            OVERDUE RENTALS
                                        ================================= */}

                                        <div className="module2-notification-section">


                                            <div className="module2-notification-section-title overdue">

                                                <span>
                                                    ⚠️
                                                </span>

                                                <span>
                                                    Overdue Rentals
                                                </span>

                                                <span className="module2-notification-section-count">

                                                    {
                                                        overdueRentals.length
                                                    }

                                                </span>

                                            </div>


                                            {overdueRentals.length === 0 ? (

                                                <div className="module2-notification-no-items">

                                                    No overdue rentals

                                                </div>

                                            ) : (

                                                overdueRentals.map(
                                                    rental => (

                                                        <div
                                                            className="module2-notification-item overdue"
                                                            key={
                                                                `overdue-${rental.rentalId}`
                                                            }
                                                            role="button"
                                                            tabIndex={0}
                                                            onClick={() =>
                                                                handleNotificationItemClick(
                                                                    rental.rentalId,
                                                                    rental.customerId
                                                                )
                                                            }
                                                            onKeyDown={event => {
                                                                if (
                                                                    event.key === "Enter" ||
                                                                    event.key === " "
                                                                ) {
                                                                    handleNotificationItemClick(
                                                                        rental.rentalId,
                                                                        rental.customerId
                                                                    );
                                                                }
                                                            }}
                                                        >

                                                            <div className="module2-notification-item-icon overdue">

                                                                !

                                                            </div>


                                                            <div className="module2-notification-item-content">

                                                                <strong>
                                                                    Rental #{rental.rentalId}
                                                                </strong>

                                                                <span>
                                                                    Customer #{rental.customerId}
                                                                </span>

                                                                <small>

                                                                    Due:{" "}

                                                                    {
                                                                        formatDate(
                                                                            rental.dueDate
                                                                        )
                                                                    }

                                                                </small>

                                                            </div>

                                                        </div>

                                                    )
                                                )

                                            )}

                                        </div>


                                        {/* =================================
                                            DUE SOON RENTALS
                                        ================================= */}

                                        <div className="module2-notification-section">


                                            <div className="module2-notification-section-title due-soon">

                                                <span>
                                                    ⏰
                                                </span>

                                                <span>
                                                    Due Soon
                                                </span>

                                                <span className="module2-notification-section-count">

                                                    {
                                                        dueSoonRentals.length
                                                    }

                                                </span>

                                            </div>


                                            {dueSoonRentals.length === 0 ? (

                                                <div className="module2-notification-no-items">

                                                    No rentals due soon

                                                </div>

                                            ) : (

                                                dueSoonRentals.map(
                                                    rental => (

                                                        <div
                                                            className="module2-notification-item due-soon"
                                                            key={`due-soon-${rental.rentalId}`}
                                                            role="button"
                                                            tabIndex={0}
                                                            onClick={() =>
                                                                handleNotificationItemClick(
                                                                    rental.rentalId,
                                                                    rental.customerId
                                                                )
                                                            }
                                                            onKeyDown={event => {
                                                                if (
                                                                    event.key === "Enter" ||
                                                                    event.key === " "
                                                                ) {
                                                                    handleNotificationItemClick(
                                                                        rental.rentalId,
                                                                        rental.customerId
                                                                    );
                                                                }
                                                            }}
                                                        >

                                                            <div className="module2-notification-item-icon due-soon">
                                                                ⏰
                                                            </div>


                                                            <div className="module2-notification-item-content">

                                                                <strong>
                                                                    Rental #{rental.rentalId}
                                                                </strong>

                                                                <span>
                                                                    Customer #{rental.customerId}
                                                                </span>

                                                                <small>

                                                                    Due:{" "}

                                                                    {
                                                                        formatDate(
                                                                            rental.dueDate
                                                                        )
                                                                    }

                                                                </small>

                                                            </div>

                                                        </div>

                                                    )
                                                )

                                            )}

                                        </div>


                                    </>

                                )}

                            </div>

                        )}

                    </div>


                </div>


                {/* =====================================
                    CURRENT MODULE 2 PAGE
                ===================================== */}

                <div className="module2-page-content">

                    <Outlet />

                </div>


            </main>


        </div>

    );
}


export default Module2Layout;