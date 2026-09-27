const BASE_URL = "http://localhost:8081/api";

export async function getReturns() {

    const response = await fetch(`${BASE_URL}/returns`);

    if (!response.ok) {
        throw new Error("Failed to load returns");
    }

    return response.json();
}


export async function createReturn(data) {

    const response = await fetch(`${BASE_URL}/returns`, {
        method: "POST",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify(data)
    });

    if (!response.ok) {
        throw new Error("Failed to create return");
    }

    return response.json();
}


export async function getDamages() {

    const response = await fetch(`${BASE_URL}/damages`);

    if (!response.ok) {
        throw new Error("Failed to load damage records");
    }

    return response.json();
}


export async function createDamage(data) {

    const response = await fetch(`${BASE_URL}/damages`, {
        method: "POST",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify(data)
    });

    if (!response.ok) {
        throw new Error("Failed to create damage record");
    }

    return response.json();
}


export async function getLostItems() {

    const response =
        await fetch(`${BASE_URL}/lost-items`);

    if (!response.ok) {
        throw new Error("Failed to load lost items");
    }

    return response.json();
}


export async function createLostItem(data) {

    const response =
        await fetch(`${BASE_URL}/lost-items`, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(data)
        });

    if (!response.ok) {
        throw new Error("Failed to create lost item");
    }

    return response.json();
}


export async function getSettlements() {

    const response =
        await fetch(`${BASE_URL}/settlements`);

    if (!response.ok) {
        throw new Error("Failed to load settlements");
    }

    return response.json();
}


export async function createSettlement(data) {

    const response =
        await fetch(`${BASE_URL}/settlements`, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(data)
        });

    if (!response.ok) {
        throw new Error("Failed to create settlement");
    }

    return response.json();
}