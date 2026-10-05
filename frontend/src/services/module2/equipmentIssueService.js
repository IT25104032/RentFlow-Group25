const API_BASE_URL = "http://localhost:8081/api";

/*
 * Get active rentals.
 */
export async function getActiveRentalsForIssue(companyId) {
    const response = await fetch(
        `${API_BASE_URL}/rentals/active?companyId=${companyId}`
    );

    if (!response.ok) {
        throw new Error("Failed to load active rentals.");
    }

    return response.json();
}


/*
 * Get all rental items belonging to a rental.
 */
export async function getRentalItemsForIssue(rentalId) {
    const response = await fetch(
        `${API_BASE_URL}/rental-items/rental/${rentalId}`
    );

    if (!response.ok) {
        throw new Error("Failed to load rental items.");
    }

    return response.json();
}


/*
 * Issue one rental item.
 */
export async function issueRentalItem(
    rentalItemId,
    rentalId
) {
    const response = await fetch(
        `${API_BASE_URL}/rental-items/${rentalItemId}/issue?rentalId=${rentalId}`,
        {
            method: "POST"
        }
    );

    if (!response.ok) {
        throw new Error(
            "Failed to issue the rental item."
        );
    }

    return response.json();
}