import { Route } from "react-router-dom";

import Module4Layout from "../components/module4/Module4Layout";
import ReturnDashboard from "../pages/module4/ReturnDashboard";
import ProcessReturn from "../pages/module4/ProcessReturn";
import ReturnHistory from "../pages/module4/ReturnHistory";
import ReturnDetails from "../pages/module4/ReturnDetails";
import DamageRecords from "../pages/module4/DamageRecords";
import LostItems from "../pages/module4/LostItems";
import Settlements from "../pages/module4/Settlements";
import SettlementDetail from "../pages/module4/SettlementDetail";

// =========================================================
// MODULE 4 (IT25104066) - returns, damage, lost items, settlement
// Only Member 4 edits this file.
// Every page sits inside Module4Layout, which adds the Module 4
// navigation bar above the page. (In the integrated app this is also
// wrapped in the shared ProtectedRoute login check.)
// =========================================================

const Module4Routes = (
    <Route element={<Module4Layout />}>
        <Route path="/returns" element={<ReturnDashboard />} />
        <Route path="/returns/new" element={<ProcessReturn />} />
        <Route path="/returns/history" element={<ReturnHistory />} />
        <Route path="/returns/damages" element={<DamageRecords />} />
        <Route path="/returns/lost" element={<LostItems />} />
        <Route path="/returns/settlements" element={<Settlements />} />
        <Route path="/returns/settlements/:rentalId" element={<SettlementDetail />} />
        <Route path="/returns/:returnId" element={<ReturnDetails />} />
    </Route>
);

export default Module4Routes;
