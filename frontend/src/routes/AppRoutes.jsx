import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";

import RegisterRenter from "../pages/module2/RegisterRenter";
import CreateRental from "../pages/module2/CreateRental";

function AppRoutes() {

    return (
        <Routes>

            {/* Module 2 - Renter Management */}
            <Route
                path="/renters"
                element={<RegisterRenter />}
            />

            {/* Module 2 - Rental Details */}
            <Route
                path="/rentals/create"
                element={<CreateRental />}
            />

            {/* Default page */}
            <Route
                path="/"
                element={
                    <Navigate
                        to="/renters"
                        replace
                    />
                }
            />

        </Routes>
    );
}

export default AppRoutes;