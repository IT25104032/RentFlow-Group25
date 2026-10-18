// Send the browser's authenticated session cookie with every backend request.
// Required because the React development server and Spring Boot use different origins.
const authenticatedFetch = (url, options = {}) =>
    fetch(url, { ...options, credentials: "include" });

const API_BASE_URL = 'http://localhost:8081/api/v1';

// Shows the backend's message if there is one, otherwise a fallback
const handleResponse = async (response, fallbackMessage) => {
    if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        throw new Error(errorData?.message || fallbackMessage);
    }
    return response.json();
};

export const getDeposits = async () => {
    const response = await authenticatedFetch(`${API_BASE_URL}/deposits`);
    return handleResponse(response, 'Failed to load security deposits.');
};

export const receiveDeposit = async (depositId, amountReceived) => {
    const response = await authenticatedFetch(`${API_BASE_URL}/deposits/${depositId}/receive`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ amountReceived })
    });
    return handleResponse(response, 'Failed to record deposit.');
};

export const refundDeposit = async (depositId, amountDeducted) => {
    const response = await authenticatedFetch(`${API_BASE_URL}/deposits/${depositId}/refund`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ amountDeducted })
    });
    return handleResponse(response, 'Failed to process refund.');
};
