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

// Post a new line item charge
export const addCharge = async (chargeData) => {
    const response = await fetch('http://localhost:8081/api/v1/charges', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(chargeData),
    });
    if (!response.ok) {
        throw new Error('Failed to add charge');
    }
    return response.json();
};

// Trigger invoice balance recalculation
export const recalculateInvoice = async (invoiceId) => {
    const response = await fetch(`http://localhost:8081/api/v1/invoices/${invoiceId}/recalculate`, {
        method: 'POST',
    });
    if (!response.ok) {
        throw new Error('Failed to recalculate invoice totals');
    }
    return response.json();
};

// Fetch all itemized charges for a specific invoice
export const getChargesByInvoice = async (invoiceId, rentalId) => {
    const baseUrl = 'http://localhost:8081/api/v1';

    try {
        const response = await fetch(`${baseUrl}/charges/invoice/${invoiceId}`);
        if (response.ok) {
            const data = await response.json();
            if (Array.isArray(data) && data.length > 0) return data;
        }
    } catch (e) {
        console.warn("Invoice ID charge query failed, falling back to Rental ID...", e);
    }

    const response = await fetch(`${baseUrl}/charges/rental/${rentalId}`);
    if (!response.ok) {
        throw new Error(`Server returned HTTP status ${response.status}`);
    }
    return response.json();
};

