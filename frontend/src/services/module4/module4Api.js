const RETURNS_URL =
    "http://localhost:8081/api/returns";

const DAMAGES_URL =
    "http://localhost:8081/api/damages";


async function readJsonOrThrow(
    response,
    fallbackMessage
) {

    if (!response.ok) {

        let message = fallbackMessage;

        try {

            const data =
                await response.json();

            if (data?.message) {
                message = data.message;
            }

        } catch {
            // Keep fallback message.
        }

        throw new Error(message);
    }

    return response.json();
}


export async function getAllReturns() {

    const response =
        await fetch(RETURNS_URL);

    return readJsonOrThrow(
        response,
        "Failed to load returns."
    );
}


export async function getReturnById(
    returnId
) {

    const response =
        await fetch(
            `${RETURNS_URL}/${returnId}`
        );

    return readJsonOrThrow(
        response,
        "Failed to load return."
    );
}


export async function getReturnItems(
    returnId
) {

    const response =
        await fetch(
            `${RETURNS_URL}/${returnId}/items`
        );

    return readJsonOrThrow(
        response,
        "Failed to load return items."
    );
}


export async function searchRentals(
    search
) {

    const response =
        await fetch(
            `${RETURNS_URL}/search?search=${encodeURIComponent(search)}`
        );

    return readJsonOrThrow(
        response,
        "Failed to search rentals."
    );
}


export async function getRentalForReturn(
    rentalId
) {

    const response =
        await fetch(
            `${RETURNS_URL}/rental/${rentalId}`
        );

    return readJsonOrThrow(
        response,
        "Failed to load rental details."
    );
}


export async function processReturn(
    returnData
) {

    const response =
        await fetch(
            `${RETURNS_URL}/process`,
            {
                method: "POST",
                headers: {
                    "Content-Type":
                        "application/json"
                },
                body:
                    JSON.stringify(
                        returnData
                    )
            }
        );

    return readJsonOrThrow(
        response,
        "Failed to process return."
    );
}


export async function createDamageRecord(
    damageData
) {

    const response =
        await fetch(
            DAMAGES_URL,
            {
                method: "POST",
                headers: {
                    "Content-Type":
                        "application/json"
                },
                body:
                    JSON.stringify(
                        damageData
                    )
            }
        );

    return readJsonOrThrow(
        response,
        "Failed to create damage record."
    );
}


export async function getAllDamages() {

    const response =
        await fetch(
            DAMAGES_URL
        );

    return readJsonOrThrow(
        response,
        "Failed to load damage records."
    );
}
