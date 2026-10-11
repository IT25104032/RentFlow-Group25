const API_BASE_URL = 'http://localhost:8081/api/v1/reports';

const handleResponse = async (response, fallbackMessage) => {
    if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        throw new Error(errorData?.message || fallbackMessage);
    }
    return response.json();
};

// Financial summary of one rental
export const getRentalSummary = async (rentalId) => {
    const response = await fetch(`${API_BASE_URL}/rental/${rentalId}`);
    return handleResponse(response, 'Failed to load the rental summary.');
};

// Type = OUTSTANDING | OVERDUE | PAYMENTS_BY_METHOD | REVENUE, dates as yyyy-mm-dd
export const getReport = async (type, from, to) => {
    const params = new URLSearchParams({ type });
    if (from) params.append('from', from);
    if (to) params.append('to', to);
    const response = await fetch(`${API_BASE_URL}?${params.toString()}`);
    return handleResponse(response, 'Failed to load the report.');
};
