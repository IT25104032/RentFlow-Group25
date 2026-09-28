const API_BASE_URL =
    "http://localhost:8081/api/customer-secondary-contacts";


/*
 * Get the existing secondary contact
 * for a customer.
 */
export async function getSecondaryContact(customerId) {

    const response = await fetch(
        `${API_BASE_URL}/customer/${customerId}`
    );


    /*
     * 404 means this customer does not
     * have a secondary contact yet.
     */
    if (response.status === 404) {
        return null;
    }


    if (!response.ok) {
        throw new Error(
            "Failed to get secondary contact"
        );
    }


    return await response.json();
}


/*
 * Create a new secondary contact.
 */
export async function createSecondaryContact(
    contactData
) {

    const response = await fetch(
        API_BASE_URL,
        {
            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(contactData)
        }
    );


    if (!response.ok) {
        throw new Error(
            "Failed to create secondary contact"
        );
    }


    return await response.json();
}


/*
 * Update an existing secondary contact.
 */
export async function updateSecondaryContact(
    secondaryContactId,
    customerId,
    contactData
) {

    const response = await fetch(
        `${API_BASE_URL}/${secondaryContactId}?customerId=${customerId}`,
        {
            method: "PUT",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(contactData)
        }
    );


    if (!response.ok) {
        throw new Error(
            "Failed to update secondary contact"
        );
    }


    return await response.json();
}