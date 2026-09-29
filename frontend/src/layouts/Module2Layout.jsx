import { Outlet } from "react-router-dom";

import "./Module2Layout.css";


function Module2Layout() {

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


                    <div className="module2-sidebar-item active">

                        <span className="module2-sidebar-icon">
                            📋
                        </span>

                        <span>
                            Create Rental
                        </span>

                    </div>


                    <div className="module2-sidebar-item">

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


                    <div className="module2-sidebar-item">

                        <span className="module2-sidebar-icon">
                            🔧
                        </span>

                        <span>
                            Issue Equipment
                        </span>

                    </div>


                    <div className="module2-sidebar-item">

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
                CURRENT MODULE 2 PAGE
            ===================================== */}

            <main className="module2-layout-content">

                <Outlet />

            </main>

        </div>
    );
}


export default Module2Layout;