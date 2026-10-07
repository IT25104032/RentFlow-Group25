import {
    Navigate,
    Route,
    Routes
} from "react-router-dom";

import ReturnDashboard
    from "../pages/module4/ReturnDashboard";

import ProcessReturn
    from "../pages/module4/ProcessReturn";

import ReturnDetails
    from "../pages/module4/ReturnDetails";

import DamageAssessment
    from "../pages/module4/DamageAssessment";


function Module4Routes() {

    return (

        <Routes>

            <Route
                path="/"
                element={
                    <Navigate
                        to="/returns"
                        replace
                    />
                }
            />

            <Route
                path="/returns"
                element={
                    <ReturnDashboard />
                }
            />

            <Route
                path="/returns/new"
                element={
                    <ProcessReturn />
                }
            />

            <Route
                path="/returns/:returnId/damages"
                element={
                    <DamageAssessment />
                }
            />

            <Route
                path="/returns/:returnId"
                element={
                    <ReturnDetails />
                }
            />

        </Routes>
    );
}

export default Module4Routes;