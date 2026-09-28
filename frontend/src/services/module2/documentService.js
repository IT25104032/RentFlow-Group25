const API_BASE_URL =
    "http://localhost:8081/api/customer-documents";


/*
 * Get all identification documents
 * belonging to a customer.
 */
export async function getDocumentsByCustomerId(
    customerId
) {

    const response = await fetch(
        `${API_BASE_URL}/customer/${customerId}`
    );


    if (!response.ok) {

        throw new Error(
            "Failed to get identification documents"
        );
    }


    return await response.json();
}


/*
 * Create a new identification document.
 */
export async function createDocument(
    documentData
) {

    const response = await fetch(
        API_BASE_URL,
        {
            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body:
                JSON.stringify(documentData)
        }
    );


    if (!response.ok) {

        throw new Error(
            "Failed to create identification document"
        );
    }


    return await response.json();
}


/*
 * Update an existing identification document.
 */
export async function updateDocument(
    documentId,
    customerId,
    documentData
) {

    const response = await fetch(
        `${API_BASE_URL}/${documentId}?customerId=${customerId}`,
        {
            method: "PUT",

            headers: {
                "Content-Type": "application/json"
            },

            body:
                JSON.stringify(documentData)
        }
    );


    if (!response.ok) {

        throw new Error(
            "Failed to update identification document"
        );
    }


    return await response.json();
}