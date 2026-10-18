import {
    BrowserRouter,
    Routes,
    Route,
    Navigate,
    useLocation,
    Outlet
} from "react-router-dom";

import AppLayout from "../layouts/AppLayout";

const getRole = (user) => String(user?.role || "")
    .replace(/^ROLE_/, "").toUpperCase();

function RoleGate({ user, allowed, children }) {
    const role = getRole(user);
    if (!allowed.includes(role)) {
        const home = ["COMPULIN_ADMIN", "COMPANY_ADMIN", "RENTAL_OFFICER"].includes(role)
            ? "/dashboard" : "/";
        return <Navigate to={home} replace />;
    }
    return children;
}

function Module4Gate({ user, children }) {
    const { pathname } = useLocation();
    const role = getRole(user);
    if (role === "RENTAL_OFFICER" ||
        (role === "COMPANY_ADMIN" && pathname === "/returns/history")) {
        return children;
    }
    return <Navigate to={role === "COMPULIN_ADMIN" ? "/dashboard" : "/returns/history"} replace />;
}

// MODULE 1
import CompanyManagement from "../components/CompanyManagement";
import Users from "../pages/Users";
import EquipmentCategories from "../pages/EquipmentCategories";
import EquipmentManagement from "../pages/EquipmentManagement";
import CompanyProfile from "../pages/CompanyProfile";
import Dashboard from "../pages/Dashboard";
import ChangePassword from "../pages/ChangePassword";

// MODULE 2
import RegisterRenter from "../pages/module2/RegisterRenter";
import CreateRental from "../pages/module2/CreateRental";
import EquipmentSelection from "../pages/module2/EquipmentSelection";
import ReviewRental from "../pages/module2/ReviewRental";
import RentalHistory from "../pages/module2/RentalHistory";
import RentalReports from "../pages/module2/RentalReports";
import RentalExtension from "../pages/module2/RentalExtension";
import IssueEquipment from "../pages/module2/IssueEquipment";

// MODULE 3
import PaymentManagement from "../pages/module3/PaymentManagement";
import SecurityDeposits from "../pages/module3/SecurityDeposits";
import FinancialReports from "../pages/module3/FinancialReports";

// MODULE 4
import Module4Routes from "./Module4Routes";

function AppRoutes({ user, onLogout }) {
    return (
        <BrowserRouter>
            <Routes>

                {/* SHARED APPLICATION LAYOUT */}
                <Route
                    element={
                        <AppLayout
                            user={user}
                            onLogout={onLogout}
                        />
                    }
                >

                    {/* MODULE 1 ROUTES */}
                    <Route path="/dashboard" element={
                        <RoleGate user={user} allowed={["COMPULIN_ADMIN", "COMPANY_ADMIN", "RENTAL_OFFICER"]}>
                            <Dashboard user={user} />
                        </RoleGate>
                    } />
                    <Route path="/company-profile" element={
                        <RoleGate user={user} allowed={["COMPANY_ADMIN"]}>
                            <CompanyProfile />
                        </RoleGate>
                    } />
                    <Route path="/companies" element={
                        <RoleGate user={user} allowed={["COMPULIN_ADMIN"]}>
                            <CompanyManagement user={user} />
                        </RoleGate>
                    } />
                    <Route path="/users" element={
                        <RoleGate user={user} allowed={["COMPANY_ADMIN"]}>
                            <Users user={user} />
                        </RoleGate>
                    } />
                    <Route path="/equipment-categories" element={
                        <RoleGate user={user} allowed={["COMPANY_ADMIN", "RENTAL_OFFICER"]}>
                            <EquipmentCategories user={user} />
                        </RoleGate>
                    } />
                    <Route path="/equipment" element={
                        <RoleGate user={user} allowed={["COMPANY_ADMIN", "RENTAL_OFFICER"]}>
                            <EquipmentManagement user={user} />
                        </RoleGate>
                    } />

                    <Route path="/change-password" element={
                            <RoleGate user={user} allowed={["COMPULIN_ADMIN", "COMPANY_ADMIN", "RENTAL_OFFICER"]}
                            >
                                <ChangePassword user={user} />
                            </RoleGate>
                        }/>

                    {/* MODULE 2 ROUTES */}

                    <Route
                        path="/"
                        element={<Navigate to="/dashboard" replace />}
                    />

                    <Route
                        path="/renters"
                        element={<RoleGate user={user} allowed={["RENTAL_OFFICER"]}><RegisterRenter /></RoleGate>}
                    />

                    <Route
                        path="/rentals/create"
                        element={<RoleGate user={user} allowed={["RENTAL_OFFICER"]}><CreateRental /></RoleGate>}
                    />

                    <Route
                        path="/rentals/equipment"
                        element={<RoleGate user={user} allowed={["RENTAL_OFFICER"]}><EquipmentSelection /></RoleGate>}
                    />

                    <Route
                        path="/rentals/review"
                        element={<RoleGate user={user} allowed={["RENTAL_OFFICER"]}><ReviewRental /></RoleGate>}
                    />

                    <Route
                        path="/rental-history"
                        element={<RoleGate user={user} allowed={["COMPANY_ADMIN", "RENTAL_OFFICER"]}><RentalHistory /></RoleGate>}
                    />

                    <Route
                        path="/rental-reports"
                        element={<RoleGate user={user} allowed={["COMPANY_ADMIN", "RENTAL_OFFICER"]}><RentalReports /></RoleGate>}
                    />

                    <Route
                        path="/rental-extension"
                        element={<RoleGate user={user} allowed={["RENTAL_OFFICER"]}><RentalExtension /></RoleGate>}
                    />

                    <Route
                        path="/issue-equipment"
                        element={<RoleGate user={user} allowed={["RENTAL_OFFICER"]}><IssueEquipment /></RoleGate>}
                    />

                    {/* MODULE 3 ROUTES */}

                    <Route
                        path="/payments"
                        element={<RoleGate user={user} allowed={["RENTAL_OFFICER"]}><PaymentManagement /></RoleGate>}
                    />

                    <Route
                        path="/security-deposits"
                        element={<RoleGate user={user} allowed={["RENTAL_OFFICER"]}><SecurityDeposits /></RoleGate>}
                    />

                    <Route
                        path="/financial-reports"
                        element={<RoleGate user={user} allowed={["COMPANY_ADMIN", "RENTAL_OFFICER"]}><FinancialReports /></RoleGate>}
                    />

                    {/* MODULE 4 ROUTES */}
                    <Route element={<Module4Gate user={user}><Outlet /></Module4Gate>}>
                        {Module4Routes}
                    </Route>

                </Route>

            </Routes>
        </BrowserRouter>
    );
}

export default AppRoutes;
