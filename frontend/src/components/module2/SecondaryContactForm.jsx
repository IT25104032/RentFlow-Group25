import { useState } from "react";

function SecondaryContactForm() {
    const [contactData, setContactData] = useState({
        contactName: "",
        relationship: "",
        phoneNumber: "",
        alternatePhone: "",
        email: "",
        address: "",
        notes: ""
    });

    function handleChange(event) {
        const { name, value } = event.target;

        setContactData({
            ...contactData,
            [name]: value
        });
    }

    function clearForm() {
        setContactData({
            contactName: "",
            relationship: "",
            phoneNumber: "",
            alternatePhone: "",
            email: "",
            address: "",
            notes: ""
        });
    }

    return (
        <div>
            <div className="module2-card-header">
                <div className="module2-section-number">03</div>

                <div>
                    <h2>Secondary Contact</h2>
                    <p className="module2-card-description">
                        Add an optional secondary contact for the renter.
                    </p>
                </div>
            </div>

            <div className="module2-form">
                <div className="module2-form-row">
                    <div className="module2-form-group">
                        <label>Contact Name</label>

                        <input
                            type="text"
                            name="contactName"
                            value={contactData.contactName}
                            onChange={handleChange}
                            placeholder="Enter contact name"
                        />
                    </div>

                    <div className="module2-form-group">
                        <label>Relationship</label>

                        <input
                            type="text"
                            name="relationship"
                            value={contactData.relationship}
                            onChange={handleChange}
                            placeholder="e.g. Brother, Manager"
                        />
                    </div>
                </div>

                <div className="module2-form-row">
                    <div className="module2-form-group">
                        <label>Phone Number</label>

                        <input
                            type="text"
                            name="phoneNumber"
                            value={contactData.phoneNumber}
                            onChange={handleChange}
                            placeholder="Enter phone number"
                        />
                    </div>

                    <div className="module2-form-group">
                        <label>Alternate Phone</label>

                        <input
                            type="text"
                            name="alternatePhone"
                            value={contactData.alternatePhone}
                            onChange={handleChange}
                            placeholder="Optional alternate number"
                        />
                    </div>
                </div>

                <div className="module2-form-row">
                    <div className="module2-form-group">
                        <label>Email</label>

                        <input
                            type="email"
                            name="email"
                            value={contactData.email}
                            onChange={handleChange}
                            placeholder="Enter email address"
                        />
                    </div>

                    <div className="module2-form-group">
                        <label>Address</label>

                        <input
                            type="text"
                            name="address"
                            value={contactData.address}
                            onChange={handleChange}
                            placeholder="Enter address"
                        />
                    </div>
                </div>

                <div className="module2-form-group">
                    <label>Notes</label>

                    <input
                        type="text"
                        name="notes"
                        value={contactData.notes}
                        onChange={handleChange}
                        placeholder="Optional notes"
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
                </div>
            </div>
        </div>
    );
}

export default SecondaryContactForm;
