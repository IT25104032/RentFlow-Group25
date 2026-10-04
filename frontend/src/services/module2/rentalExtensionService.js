const API_BASE_URL = "http://localhost:8081/api";


/*
 * Get all active rentals for the company.
 */
export async function getActiveRentals(companyId) {

    const response = await fetch(
        `${API_BASE_URL}/rentals/active?companyId=${companyId}`
    );

    if (!response.ok) {
        throw new Error(
            "Failed to load active rentals."
        );
    }

    return response.json();
}


/*
 * Create a rental extension.
 */
export async function createRentalExtension(
    extensionData
) {

    const response = await fetch(
        `${API_BASE_URL}/rental-extensions`,
        {
            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(
                extensionData
            )
        }
    );

    if (!response.ok) {
        throw new Error(
            "Failed to create rental extension."
        );
    }

    return response.json();
}


/*
 * Get extension history for a rental.
 */
export async function getRentalExtensionHistory(
    rentalId
) {

    const response = await fetch(
        `${API_BASE_URL}/rental-extensions/rental/${rentalId}`
    );

    if (!response.ok) {
        throw new Error(
            "Failed to load rental extension history."
        );
    }

    return response.json();
}