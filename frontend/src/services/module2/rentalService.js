const API_BASE_URL = "http://localhost:8081/api/rentals";

/*
 * Create a new rental.
 */
export async function createRental(rentalData) {
    const response = await fetch(API_BASE_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(rentalData)
    });

    if (!response.ok) {
        throw new Error("Failed to create rental");
    }

    return await response.json();
}


/*
 * Get one rental by rental ID and company ID.
 */
export async function getRental(rentalId, companyId) {
    const response = await fetch(
        `${API_BASE_URL}/${rentalId}?companyId=${companyId}`
    );

    if (!response.ok) {
        throw new Error("Failed to fetch rental");
    }

    return await response.json();
}


/*
 * Get rental history for a specific renter/customer.
 */
export async function getCustomerRentalHistory(
    customerId,
    companyId
) {
    const response = await fetch(
        `${API_BASE_URL}/customer/${customerId}?companyId=${companyId}`
    );

    if (!response.ok) {
        throw new Error("Failed to fetch rental history");
    }

    return await response.json();
}