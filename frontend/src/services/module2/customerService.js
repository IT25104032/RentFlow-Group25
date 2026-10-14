const API_BASE_URL =
    "http://localhost:8081/api/customers";

/*
 * Create a new renter.
 */
export async function createCustomer(customerData) {

    const response = await fetch(API_BASE_URL, {
        method: "POST",
        credentials: "include",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify(customerData)
    });

    if (!response.ok) {
        throw new Error("Failed to register renter.");
    }

    return await response.json();
}

/*
 * Get all customers belonging to a company.
 */
export async function getAllCustomers(companyId) {

    const response = await fetch(
        `${API_BASE_URL}?companyId=${companyId}`,
        {
            credentials: "include"
        }
    );

    if (!response.ok) {
        throw new Error("Failed to load renters.");
    }

    return await response.json();
}

/*
 * Search for existing renters.
 */
export async function searchCustomers({
                                          companyId,
                                          name = null,
                                          phone = null
                                      }) {

    const params = new URLSearchParams();

    params.append("companyId", companyId);

    if (name) {
        params.append("name", name);
    }

    if (phone) {
        params.append("phone", phone);
    }

    const response = await fetch(
        `${API_BASE_URL}/search?${params.toString()}`,
        {
            credentials: "include"
        }
    );

    if (!response.ok) {
        throw new Error("Failed to search renters.");
    }

    return await response.json();
}

/*
 * Update an existing renter.
 */
export async function updateCustomer(
    customerId,
    companyId,
    customerData
) {

    const response = await fetch(
        `${API_BASE_URL}/${customerId}?companyId=${companyId}`,
        {
            method: "PUT",
            credentials: "include",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(customerData)
        }
    );

    if (!response.ok) {
        throw new Error("Failed to update renter.");
    }

    return await response.json();
}

/*
 * Get a single renter by ID.
 */
export async function getCustomer(
    customerId,
    companyId
) {

    const response = await fetch(
        `${API_BASE_URL}/${customerId}?companyId=${companyId}`,
        {
            credentials: "include"
        }
    );

    if (!response.ok) {
        throw new Error("Failed to get renter.");
    }

    return await response.json();
}
