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

/* Register renter, ID document and secondary contact in one transaction. */
export async function createCompleteRenter({ customer, contact, document }) {
    const data = new FormData();
    data.append("customer", new Blob([JSON.stringify(customer)], { type: "application/json" }));
    data.append("contact", new Blob([JSON.stringify(contact)], { type: "application/json" }));
    data.append("documentType", document.documentType);
    data.append("documentNumber", document.documentNumber);
    data.append("documentFile", document.documentFile);
    if (document.expiryDate) data.append("expiryDate", document.expiryDate);
    if (document.notes) data.append("notes", document.notes);

    const response = await fetch(`${API_BASE_URL}/complete-registration`, {
        method: "POST",
        credentials: "include",
        body: data
    });
    if (!response.ok) {
        let message = `Complete renter registration failed (HTTP ${response.status}).`;
        try {
            const body = await response.json();
            if (body.message) message += ` ${body.message}`;
            else if (body.detail) message += ` ${body.detail}`;
        } catch { /* empty or non-JSON response */ }
        throw new Error(message);
    }
    return response.json();
}
