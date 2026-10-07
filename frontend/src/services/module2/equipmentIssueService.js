const API_BASE_URL = "http://localhost:8081/api";

/*
 * Get all DRAFT rentals available for the issue stage.
 */
export async function getActiveRentalsForIssue(companyId) {
    const response = await fetch(
        `${API_BASE_URL}/rentals/for-issue?companyId=${companyId}`
    );

    if (!response.ok) {
        throw new Error("Failed to load rentals available for issue.");
    }

    return response.json();
}

/*
 * Issue the complete rental.
 * The backend changes the rental from DRAFT to ACTIVE
 * and marks all selected rental items as ISSUED.
 */
export async function issueRental(rentalId, companyId) {
    const response = await fetch(
        `${API_BASE_URL}/rentals/${rentalId}/issue?companyId=${companyId}`,
        {
            method: "POST"
        }
    );

    if (!response.ok) {
        throw new Error("Failed to issue rental.");
    }

    return response.json();
}

/*
 * Cancel a DRAFT rental.
 * The rental remains in the database with status CANCELLED.
 */
export async function cancelRental(rentalId, companyId) {
    const response = await fetch(
        `${API_BASE_URL}/rentals/${rentalId}/cancel?companyId=${companyId}`,
        {
            method: "POST"
        }
    );

    if (!response.ok) {
        throw new Error("Failed to cancel rental.");
    }

    return response.json();
}
