const RETURNS_URL = "http://localhost:8081/api/returns";

export async function getAllReturns() {

    const response = await fetch(RETURNS_URL);

    if (!response.ok) {
        throw new Error("Failed to load returns");
    }

    return response.json();
}

export async function getReturnById(id) {

    const response = await fetch(`${RETURNS_URL}/${id}`);

    if (!response.ok) {
        throw new Error("Failed to load return");
    }

    return response.json();
}

export async function createReturn(returnData) {

    const response = await fetch(RETURNS_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(returnData)
    });

    if (!response.ok) {
        throw new Error("Failed to create return");
    }

    return response.json();
}

export async function getReturnItems(returnId) {

    const response =
        await fetch(`${RETURNS_URL}/${returnId}/items`);

    if (!response.ok) {
        throw new Error("Failed to load return items");
    }

    return response.json();
}