import { useState } from "react";
import { createCustomer } from "../../services/module2/customerService";

function CustomerForm({ onCustomerCreated}) {
    const [formData, setFormData] = useState({
        customerName: "",
        email: "",
        phone: "",
        address: "",
        customerType: ""
    });

    function handleChange(event) {
        const { name, value } = event.target;

        setFormData({
            ...formData,
            [name]: value
        });
    }

    function clearForm() {
        setFormData({
            customerName: "",
            email: "",
            phone: "",
            address: "",
            customerType: ""
        });
    }

    function clearCustomerType() {
        setFormData({
            ...formData,
            customerType: ""
        });
    }

    async function handleSubmit(event) {
        event.preventDefault();

        try {
            const customerData = {
                ...formData,
                companyId: 1000,
                customerStatus: "ACTIVE",
                createdBy: 3
            };

            const response = await createCustomer(customerData);

            console.log("Renter registered:", response);

            if (onCustomerCreated) {
                onCustomerCreated(response.customerId);
            }

            alert("Renter registered successfully!");

            clearForm();
        } catch (error) {
            console.error("Registration failed:", error);

            alert("Failed to register renter.");
        }
    }


    return (
        <form className="module2-form" onSubmit={handleSubmit}>
            <div className="module2-card-header">
                <div className="module2-section-number">01</div>

                <div>
                    <h2>Renter Details</h2>
                    <p className="module2-card-description">
                        Enter the basic information of the renter.
                    </p>
                </div>
            </div>

            <div className="module2-form-row">
                <div className="module2-form-group">
                    <label>
                        Renter Name
                        <span className="module2-required">*</span>
                    </label>

                    <input
                        type="text"
                        name="customerName"
                        value={formData.customerName}
                        onChange={handleChange}
                        placeholder="Enter renter name"
                        required
                    />
                </div>

                <div className="module2-form-group">
                    <label>Email</label>

                    <input
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        placeholder="Enter email address"
                    />
                </div>
            </div>

            <div className="module2-form-row">
                <div className="module2-form-group">
                    <label>
                        Phone
                        <span className="module2-required">*</span>
                    </label>

                    <input
                        type="text"
                        name="phone"
                        value={formData.phone}
                        onChange={handleChange}
                        placeholder="Enter phone number"
                        required
                    />
                </div>

                <div className="module2-form-group">
                    <label>
                        Renter Type
                        <span className="module2-required">*</span>
                    </label>

                    <div className="module2-select-wrapper">
                        <select
                            name="customerType"
                            value={formData.customerType}
                            onChange={handleChange}
                            required
                        >
                            <option value="" disabled>
                                Select renter type
                            </option>

                            <option value="INDIVIDUAL">
                                Individual
                            </option>

                            <option value="BUSINESS">
                                Business
                            </option>
                        </select>

                        <button
                            type="button"
                            className="module2-clear-selection"
                            onClick={clearCustomerType}
                        >
                            Clear
                        </button>
                    </div>
                </div>
            </div>

            <div className="module2-form-group">
                <label>Address</label>

                <input
                    type="text"
                    name="address"
                    value={formData.address}
                    onChange={handleChange}
                    placeholder="Enter renter address"
                />
            </div>

            <div className="module2-form-actions">
                <button
                    type="button"
                    className="module2-btn module2-btn-secondary"
                    onClick={clearForm}
                >
                    Clear Form
                </button>

                <button
                    type="submit"
                    className="module2-btn module2-btn-primary"
                >
                    Register Renter
                </button>
            </div>
        </form>
    );
}

export default CustomerForm;