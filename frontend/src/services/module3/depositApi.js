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
    const response = await fetch(`${API_BASE_URL}/deposits`);
    return handleResponse(response, 'Failed to load security deposits.');
};

export const createDeposit = async (rentalId, calculatedDeposit) => {
    const response = await fetch(`${API_BASE_URL}/deposits`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ rentalId, calculatedDeposit })
    });
    return handleResponse(response, `Failed to create deposit for Rental #${rentalId}. Check that the rental exists and has no deposit yet.`);
};

export const receiveDeposit = async (depositId, amountReceived) => {
    const response = await fetch(`${API_BASE_URL}/deposits/${depositId}/receive`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ amountReceived })
    });
    return handleResponse(response, 'Failed to record deposit.');
};

export const refundDeposit = async (depositId, amountDeducted) => {
    const response = await fetch(`${API_BASE_URL}/deposits/${depositId}/refund`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ amountDeducted })
    });
    return handleResponse(response, 'Failed to process refund.');
};
