
const API_BASE_URL = "http://localhost:8081/api";

// Get DRAFT rentals available for issuing.
export async function getActiveRentalsForIssue(companyId) {
    const response = await fetch(
        `${API_BASE_URL}/rentals/for-issue?companyId=${companyId}`
    );

    if (!response.ok) {
        throw new Error("Failed to load rentals available for issue.");
    }

    return response.json();
}

// Issue all selected rental items and activate the rental.
export async function issueRental(rentalId, companyId) {
    const response = await fetch(
        `${API_BASE_URL}/rentals/${rentalId}/issue?companyId=${companyId}`,
        {
            method: "POST"
        }
    );

    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "Failed to issue rental.");
    }

    return response.json();
}

// Cancel a DRAFT rental.
export async function cancelRental(rentalId, companyId) {
    const response = await fetch(
        `${API_BASE_URL}/rentals/${rentalId}/cancel?companyId=${companyId}`,
        {
            method: "POST"
        }
    );

    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "Failed to cancel rental.");
    }

    return response.json();
}

// Get the equipment items associated with a rental.
export async function getRentalItemsForIssue(rentalId) {
    const response = await fetch(
        `${API_BASE_URL}/rental-items/rental/${rentalId}`
    );

    if (!response.ok) {
        throw new Error(
            "Failed to load equipment items for this rental."
        );
    }

    return response.json();
}
