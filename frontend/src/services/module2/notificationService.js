const API_BASE_URL = "http://localhost:8081/api/rentals";


/*
 * Get rentals that are currently overdue.
 */
export async function getOverdueRentals(companyId) {
    const response = await fetch(
        `${API_BASE_URL}/overdue?companyId=${companyId}`
    );

    if (!response.ok) {
        throw new Error("Failed to fetch overdue rentals");
    }

    return await response.json();
}


/*
 * Get rentals whose due dates are within the
 * backend's due-soon window (currently 3 days).
 */
export async function getRentalsDueSoon(companyId) {
    const response = await fetch(
        `${API_BASE_URL}/due-soon?companyId=${companyId}`
    );

    if (!response.ok) {
        throw new Error("Failed to fetch rentals due soon");
    }

    return await response.json();
}