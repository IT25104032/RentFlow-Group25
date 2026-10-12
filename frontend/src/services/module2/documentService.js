const API_BASE_URL =
    "http://localhost:8081/api/customer-documents";


/*
 * Get all identification documents
 * belonging to a renter.
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
 *
 * Sends the actual file using FormData.
 */
export async function createDocument(
    documentData
) {

    const formData =
        new FormData();


    formData.append(
        "customerId",
        documentData.customerId
    );

    formData.append(
        "documentType",
        documentData.documentType
    );

    formData.append(
        "documentNumber",
        documentData.documentNumber
    );

    formData.append(
        "checkedBy",
        documentData.checkedBy
    );


    if (documentData.expiryDate) {

        formData.append(
            "expiryDate",
            documentData.expiryDate
        );
    }


    if (documentData.notes) {

        formData.append(
            "notes",
            documentData.notes
        );
    }


    /*
     * Add the actual browser File.
     */
    if (documentData.documentFile) {

        formData.append(
            "documentFile",
            documentData.documentFile
        );
    }


    const response = await fetch(
        API_BASE_URL,
        {
            method: "POST",
            body: formData
        }
    );


    if (!response.ok) {

        throw new Error(
            "Failed to save identification document"
        );
    }

    return await response.json();
}


/*
 * Update an existing identification document.
 *
 * If a new file is selected, the new file is
 * uploaded and document_copy_path is replaced.
 *
 * If no new file is selected, the existing
 * server-side document is kept.
 */
export async function updateDocument(
    documentId,
    customerId,
    documentData
) {

    const formData =
        new FormData();


    formData.append(
        "customerId",
        customerId
    );

    formData.append(
        "documentType",
        documentData.documentType
    );

    formData.append(
        "documentNumber",
        documentData.documentNumber
    );

    formData.append(
        "checkedBy",
        documentData.checkedBy
    );


    if (documentData.expiryDate) {

        formData.append(
            "expiryDate",
            documentData.expiryDate
        );
    }


    if (documentData.notes) {

        formData.append(
            "notes",
            documentData.notes
        );
    }


    /*
     * Only send a file when the user selected
     * a new identification document.
     */
    if (documentData.documentFile) {

        formData.append(
            "documentFile",
            documentData.documentFile
        );
    }


    const response = await fetch(
        `${API_BASE_URL}/${documentId}`,
        {
            method: "PUT",
            body: formData
        }
    );


    if (!response.ok) {

        throw new Error(
            "Failed to update identification document"
        );
    }

    return await response.json();
}