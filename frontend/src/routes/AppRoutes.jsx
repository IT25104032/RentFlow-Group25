import {
    BrowserRouter,
    Routes,
    Route
} from "react-router-dom";

import Module2Layout from "../layouts/Module2Layout";

import RegisterRenter from "../pages/module2/RegisterRenter";
import CreateRental from "../pages/module2/CreateRental";
import EquipmentSelection from "../pages/module2/EquipmentSelection";
import ReviewRental from "../pages/module2/ReviewRental";
import RentalHistory from "../pages/module2/RentalHistory";


function AppRoutes() {

    return (
        <BrowserRouter>

            <Routes>

                <Route element={<Module2Layout />}>

                    <Route
                        path="/"
                        element={
                            <RegisterRenter />
                        }
                    />

                    <Route
                        path="/renters"
                        element={
                            <RegisterRenter />
                        }
                    />

                    <Route
                        path="/rentals/create"
                        element={
                            <CreateRental />
                        }
                    />

                    <Route
                        path="/rentals/equipment"
                        element={
                            <EquipmentSelection />
                        }
                    />

                    <Route
                        path="/rentals/review"
                        element={
                            <ReviewRental />
                        }
                    />

                    <Route
                        path="/rental-history"
                        element={
                            <RentalHistory />
                        }
                    />

                </Route>

            </Routes>

        </BrowserRouter>
    );
}


export default AppRoutes;