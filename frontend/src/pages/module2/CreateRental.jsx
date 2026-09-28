import React, { useState } from "react";
import { useLocation } from "react-router-dom";
import { createRental } from "../../services/module2/rentalService";
import RentalStepper from "../../components/module2/RentalStepper";

const COMPANY_ID = 1000;
const USER_ID = 3;

function CreateRental({ onRentalCreated, onBack }) {

    const location = useLocation();

    const selectedRenter =
        location.state?.selectedRenter || null;

    const [startDate, setStartDate] = useState("");
    const [dueDate, setDueDate] = useState("");
    const [notes, setNotes] = useState("");

    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    /*
     * Validate the rental dates before sending
     * the data to the backend.
     */
    const validateDates = () => {

        if (!startDate) {
            setError("Please select a rental start date.");
            return false;
        }

        if (!dueDate) {
            setError("Please select a rental due date.");
            return false;
        }

        if (dueDate < startDate) {
            setError("Due date cannot be before the start date.");
            return false;
        }

        return true;
    };


    /*
     * Create the rental through the Spring Boot API.
     */
    const handleContinue = async () => {

        setError("");
        setSuccess("");

        if (!selectedRenter) {
            setError("Please select a renter first.");
            return;
        }

        if (!validateDates()) {
            return;
        }

        const rentalData = {
            companyId: COMPANY_ID,
            customerId: selectedRenter.customerId,
            createdBy: USER_ID,
            startDate: startDate,
            dueDate: dueDate,
            rentalStatus: "DRAFT",
            notes: notes.trim() || null
        };

        try {

            setSaving(true);

            const createdRental =
                await createRental(rentalData);

            setSuccess("Rental details saved successfully.");

            if (onRentalCreated) {
                onRentalCreated(createdRental);
            }

        } catch (exception) {

            setError(
                exception.message ||
                "Failed to create rental."
            );

        } finally {

            setSaving(false);
        }
    };


    /*
     * Clear only the rental details entered
     * on this page.
     */
    const handleClear = () => {

        setStartDate("");
        setDueDate("");
        setNotes("");

        setError("");
        setSuccess("");
    };


    return (
        <div className="create-rental-page">

            <div className="page-header">

                <div>
                    <h1>Create Rental</h1>

                    <p>
                        Enter the rental details for the selected renter.
                    </p>
                </div>

            </div>

            <RentalStepper activeStep={2} />

            {/* Selected Renter */}

            <div className="rental-card">

                <div className="card-header">

                    <div>
                        <h2>Selected Renter</h2>

                        <p>
                            The rental will be created for this renter.
                        </p>
                    </div>

                </div>


                {selectedRenter ? (

                    <div className="selected-renter">

                        <div className="renter-summary-row">

                            <div className="renter-summary-field">

                                <span className="field-label">
                                    Renter Name
                                </span>

                                <span className="field-value">
                                    {selectedRenter.customerName}
                                </span>

                            </div>


                            <div className="renter-summary-field">

                                <span className="field-label">
                                    Phone
                                </span>

                                <span className="field-value">
                                    {selectedRenter.phone}
                                </span>

                            </div>


                            <div className="renter-summary-field">

                                <span className="field-label">
                                    Email
                                </span>

                                <span className="field-value">
                                    {selectedRenter.email || "-"}
                                </span>

                            </div>

                        </div>

                    </div>

                ) : (

                    <div className="empty-message">
                        No renter selected.
                    </div>

                )}

            </div>


            {/* Rental Details */}

            <div className="rental-card">

                <div className="card-header">

                    <div>
                        <h2>Rental Details</h2>

                        <p>
                            Select the rental period and add any notes.
                        </p>
                    </div>

                </div>


                <div className="form-grid">

                    <div className="form-group">

                        <label htmlFor="startDate">
                            Start Date
                        </label>

                        <input
                            id="startDate"
                            type="date"
                            value={startDate}
                            onChange={(event) =>
                                setStartDate(event.target.value)
                            }
                        />

                    </div>


                    <div className="form-group">

                        <label htmlFor="dueDate">
                            Due Date
                        </label>

                        <input
                            id="dueDate"
                            type="date"
                            value={dueDate}
                            min={startDate || undefined}
                            onChange={(event) =>
                                setDueDate(event.target.value)
                            }
                        />

                    </div>


                    <div className="form-group full-width">

                        <label htmlFor="notes">
                            Notes
                        </label>

                        <textarea
                            id="notes"
                            value={notes}
                            onChange={(event) =>
                                setNotes(event.target.value)
                            }
                            placeholder="Enter any notes about this rental"
                            rows="4"
                        />

                    </div>

                </div>

            </div>


            {/* Error / Success Messages */}

            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}


            {success && (
                <div className="success-message">
                    {success}
                </div>
            )}


            {/* Actions */}

            <div className="form-actions">

                <button
                    type="button"
                    onClick={handleClear}
                    disabled={saving}
                >
                    Clear Form
                </button>


                <div className="right-actions">

                    {onBack && (
                        <button
                            type="button"
                            onClick={onBack}
                            disabled={saving}
                        >
                            Back
                        </button>
                    )}


                    <button
                        type="button"
                        onClick={handleContinue}
                        disabled={saving}
                    >
                        {saving
                            ? "Saving..."
                            : "Continue to Equipment"}
                    </button>

                </div>

            </div>

        </div>
    );
}

export default CreateRental;