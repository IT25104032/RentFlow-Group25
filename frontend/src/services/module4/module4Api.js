// =========================================================
// MODULE 4 (IT25104066) - returns, damage, lost items, settlement
// All calls go through the Vite proxy (/api -> Spring Boot :8081),
// so the login session cookie is sent and the backend knows the
// logged-in user and company.
// =========================================================

async function request(url, options = {}) {
    const response = await fetch(url, {
        credentials: "include",
        headers: options.body ? { "Content-Type": "application/json" } : undefined,
        ...options
    });

    const text = await response.text();
    let data = null;
    try {
        data = text ? JSON.parse(text) : null;
    } catch {
        data = null;
    }

    if (!response.ok) {
        throw new Error(data?.message || `Request failed (${response.status})`);
    }
    return data;
}

function query(params) {
    const q = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== "") {
            q.set(key, value);
        }
    });
    const s = q.toString();
    return s ? `?${s}` : "";
}

const post = (url, body) =>
    request(url, { method: "POST", body: body === undefined ? undefined : JSON.stringify(body) });

// ---------- returns ----------
export const getOverview = () => request("/api/returns/overview");

export const getOpenRentals = (search) =>
    request(`/api/returns/open-rentals${query({ search })}`);

export const getRentalForReturn = (rentalId, returnDate) =>
    request(`/api/returns/rental/${rentalId}${query({ returnDate })}`);

export const processReturn = (data) => post("/api/returns/process", data);

export const getReturns = (search, rentalId) =>
    request(`/api/returns${query({ search, rentalId })}`);

export const getReturnDetails = (returnId) => request(`/api/returns/${returnId}`);

// ---------- damage records ----------
export const getDamages = (status) => request(`/api/damages${query({ status })}`);
export const chargeDamage = (damageId, amount) => post(`/api/damages/${damageId}/charge`, { amount });
export const waiveDamage = (damageId) => post(`/api/damages/${damageId}/waive`);
export const markDamageRepaired = (damageId) => post(`/api/damages/${damageId}/repaired`);

// ---------- lost items ----------
export const getLostItems = (status) => request(`/api/lost-items${query({ status })}`);
export const recordLostItem = (data) => post("/api/lost-items", data);
export const chargeLostItem = (lostItemId, amount) => post(`/api/lost-items/${lostItemId}/charge`, { amount });
export const recoverLostItem = (lostItemId) => post(`/api/lost-items/${lostItemId}/recover`);

// ---------- settlement ----------
export const getSettlements = (search) => request(`/api/settlements${query({ search })}`);
export const getSettlement = (rentalId) => request(`/api/settlements/${rentalId}`);
export const settleRental = (rentalId) => post(`/api/settlements/${rentalId}`, {});
export const paySettlement = (rentalId, data) => post(`/api/settlements/${rentalId}/payments`, data);
