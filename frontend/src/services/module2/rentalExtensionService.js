const API_BASE_URL = "http://localhost:8081/api";

/*
 * Get all rentals for the extension page.
 * The page shows every rental in the company so that
 * it stays consistent with Rental History.
 */
export async function getRentalsForExtension(companyId) {
    const response = await fetch(
        `${API_BASE_URL}/rentals/for-extension?companyId=${companyId}`
    );

    if (!response.ok) {
        throw new Error("Failed to load rentals for extension.");
    }

    return response.json();
}

/*
 * Create a rental extension.
 */
export async function createRentalExtension(extensionData) {
    const response = await fetch(
        `${API_BASE_URL}/rental-extensions`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(extensionData)
        }
    );

    if (!response.ok) {
        throw new Error("Failed to create rental extension.");
    }

    return response.json();
}

/*
 * Get extension history for a rental.
 */
export async function getRentalExtensionHistory(rentalId) {
    const response = await fetch(
        `${API_BASE_URL}/rental-extensions/rental/${rentalId}`
    );

    if (!response.ok) {
        throw new Error("Failed to load rental extension history.");
    }

    return response.json();
}
