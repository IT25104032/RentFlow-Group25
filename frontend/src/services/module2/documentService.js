const API_BASE_URL = "http://localhost:8081/api/customer-documents";

export async function createDocument(documentData) {
    const response = await fetch(API_BASE_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(documentData)
    });

    if (!response.ok) {
        throw new Error("Failed to save identification document");
    }

    return await response.json();
}