const API_BASE_URL = "http://localhost:8081/api/customers";

export async function createCustomer(customerData) {
    const response = await fetch(API_BASE_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(customerData)
    });

    if (!response.ok) {
        throw new Error("Failed to register renter");
    }

    return await response.json();
}