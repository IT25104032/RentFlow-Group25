const API_BASE_URL =
    "http://localhost:8081/api/customers";


/*
 * Create a new renter.
 */
export async function createCustomer(customerData) {

    const response = await fetch(API_BASE_URL, {
        method: "POST",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify(customerData)
    });


    if (!response.ok) {
        throw new Error(
            "Failed to register renter."
        );
    }


    return await response.json();
}


/*
 * Search for existing renters.
 *
 * name  -> partial name search
 * phone -> exact phone search
 *
 * If both are supplied, the backend must
 * match BOTH conditions.
 */
export async function searchCustomers({
                                          companyId,
                                          name = null,
                                          phone = null
                                      }) {

    const params = new URLSearchParams();

    params.append(
        "companyId",
        companyId
    );


    if (name) {

        params.append(
            "name",
            name
        );
    }


    if (phone) {

        params.append(
            "phone",
            phone
        );
    }


    const response = await fetch(
        `${API_BASE_URL}/search?${params.toString()}`
    );


    if (!response.ok) {

        throw new Error(
            "Failed to search renters."
        );
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

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(customerData)
        }
    );


    if (!response.ok) {

        throw new Error(
            "Failed to update renter."
        );
    }


    return await response.json();
}


/*
 * Get one renter by ID.
 */
export async function getCustomer(
    customerId,
    companyId
) {

    const response = await fetch(
        `${API_BASE_URL}/${customerId}?companyId=${companyId}`
    );


    if (!response.ok) {

        throw new Error(
            "Failed to get renter."
        );
    }


    return await response.json();
}