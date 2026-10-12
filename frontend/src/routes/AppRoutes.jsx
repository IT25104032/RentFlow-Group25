import {
    BrowserRouter,
    Routes,
    Route
} from "react-router-dom";

import AppLayout from "../layouts/AppLayout";

// MODULE 2
import RegisterRenter from "../pages/module2/RegisterRenter";
import CreateRental from "../pages/module2/CreateRental";
import EquipmentSelection from "../pages/module2/EquipmentSelection";
import ReviewRental from "../pages/module2/ReviewRental";
import RentalHistory from "../pages/module2/RentalHistory";
import RentalExtension from "../pages/module2/RentalExtension";
import IssueEquipment from "../pages/module2/IssueEquipment";

// MODULE 3
import PaymentManagement from "../pages/module3/PaymentManagement";
import SecurityDeposits from "../pages/module3/SecurityDeposits";
import FinancialReports from "../pages/module3/FinancialReports";

// MODULE 4
import Module4Routes from "./Module4Routes";

function AppRoutes() {
    return (
        <BrowserRouter>
            <Routes>

                {/* SHARED APPLICATION LAYOUT */}
                <Route element={<AppLayout />}>

                    {/* MODULE 2 ROUTES */}

                    <Route
                        path="/"
                        element={<RegisterRenter />}
                    />

                    <Route
                        path="/renters"
                        element={<RegisterRenter />}
                    />

                    <Route
                        path="/rentals/create"
                        element={<CreateRental />}
                    />

                    <Route
                        path="/rentals/equipment"
                        element={<EquipmentSelection />}
                    />

                    <Route
                        path="/rentals/review"
                        element={<ReviewRental />}
                    />

                    <Route
                        path="/rental-history"
                        element={<RentalHistory />}
                    />

                    <Route
                        path="/rental-extension"
                        element={<RentalExtension />}
                    />

                    <Route
                        path="/issue-equipment"
                        element={<IssueEquipment />}
                    />

                    {/* MODULE 3 ROUTES */}

                    <Route
                        path="/payments"
                        element={<PaymentManagement />}
                    />

                    <Route
                        path="/security-deposits"
                        element={<SecurityDeposits />}
                    />

                    <Route
                        path="/financial-reports"
                        element={<FinancialReports />}
                    />

                    {/* MODULE 4 ROUTES */}
                    {Module4Routes}

                </Route>

            </Routes>
        </BrowserRouter>
    );
}

export default AppRoutes;
