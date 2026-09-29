const API_BASE_URL = 'http://localhost:8081/api/v1';

export const getInvoices = async () => {
    const response = await fetch(`${API_BASE_URL}/invoices`);
    if (!response.ok) throw new Error(`Server returned status ${response.status}`);
    return await response.json();
};

export const recordPayment = async (payload) => {
    const response = await fetch(`${API_BASE_URL}/payments`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Payment failed.');
    }
    return await response.json();
};