import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import RentalStepper from "../../components/module2/RentalStepper";

import "./CreateRental.css";


function CreateRental({ onBack }) {

    const location = useLocation();
    const navigate = useNavigate();

    /*
     * Get the renter selected in Step 1.
     */
    const selectedRenter =
        location.state?.selectedRenter || null;


    /*
     * Rental details entered on this page.
     */
    const [startDate, setStartDate] = useState("");
    const [dueDate, setDueDate] = useState("");
    const [notes, setNotes] = useState("");


    /*
     * UI state.
     */
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");


    /*
     * Validate the rental dates.
     */
    const validateDates = () => {

        if (!startDate) {

            setError(
                "Please select a rental start date."
            );

            return false;
        }


        if (!dueDate) {

            setError(
                "Please select a rental due date."
            );

            return false;
        }


        if (dueDate < startDate) {

            setError(
                "Due date cannot be before the start date."
            );

            return false;
        }


        return true;
    };


    /*
     * Continue to equipment selection.
     *
     * At this stage we DO NOT create the
     * rental in the backend yet.
     */
    const handleContinue = () => {

        setError("");
        setSuccess("");


        if (!selectedRenter) {

            setError(
                "Please select a renter first."
            );

            return;
        }


        if (!validateDates()) {
            return;
        }


        /*
         * Temporary rental information.
         *
         * This object stays in the frontend
         * while the user continues through
         * the rental workflow.
         */
        const rentalDetails = {

            companyId: 1000,

            customerId:
            selectedRenter.customerId,

            createdBy: 3,

            startDate:
            startDate,

            dueDate:
            dueDate,

            rentalStatus:
                "DRAFT",

            notes:
                notes.trim() || null
        };


        /*
         * Move to Step 3.
         *
         * Pass both the selected renter
         * and the rental details forward.
         */
        navigate(
            "/rentals/equipment",
            {
                state: {

                    selectedRenter:
                    selectedRenter,

                    rentalDetails:
                    rentalDetails
                }
            }
        );
    };


    /*
     * Clear only the fields on this page.
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


            {/* =====================================
                TOP NAVIGATION
            ===================================== */}

            <header className="create-rental-topbar">

                <div className="create-rental-brand">
                    RentFlow
                </div>


                <div className="create-rental-user">

                    <div className="create-rental-user-avatar">
                        <span>●</span>
                    </div>

                    <span>
                        Staff User
                    </span>

                    <span className="create-rental-chevron">
                        ⌄
                    </span>

                </div>

            </header>


            {/* =====================================
                MAIN CONTENT
            ===================================== */}

            <main className="create-rental-content">


                {/* PAGE HEADING */}

                <div className="create-rental-heading">

                    <h1>
                        Create Rental
                    </h1>

                    <p>
                        Enter the rental details for the
                        selected renter.
                    </p>

                </div>


                {/* =================================
                    RENTAL STEPPER
                ================================= */}

                <RentalStepper
                    activeStep={2}
                />


                {/* =================================
                    SELECTED RENTER
                ================================= */}

                <section className="create-rental-card">


                    <div className="create-rental-card-heading">

                        <div>

                            <h2>
                                Selected Renter
                            </h2>

                            <p>
                                The rental will be created
                                for this renter.
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
                                        {
                                            selectedRenter.customerName
                                        }
                                    </span>

                                </div>


                                <div className="renter-summary-field">

                                    <span className="field-label">
                                        Phone
                                    </span>

                                    <span className="field-value">
                                        {
                                            selectedRenter.phone
                                        }
                                    </span>

                                </div>


                                <div className="renter-summary-field">

                                    <span className="field-label">
                                        Email
                                    </span>

                                    <span className="field-value">
                                        {
                                            selectedRenter.email
                                            || "-"
                                        }
                                    </span>

                                </div>


                            </div>

                        </div>

                    ) : (

                        <div className="empty-message">

                            No renter selected.

                        </div>

                    )}

                </section>


                {/* =================================
                    RENTAL DETAILS
                ================================= */}

                <section className="create-rental-card">


                    <div className="create-rental-card-heading">

                        <div>

                            <h2>
                                Rental Details
                            </h2>

                            <p>
                                Select the rental period
                                and add any notes.
                            </p>

                        </div>

                    </div>


                    <div className="rental-form-grid">


                        {/* START DATE */}

                        <div className="rental-form-group">

                            <label htmlFor="startDate">
                                Start Date
                            </label>

                            <input
                                id="startDate"
                                type="date"
                                value={startDate}
                                onChange={(event) =>
                                    setStartDate(
                                        event.target.value
                                    )
                                }
                            />

                        </div>


                        {/* DUE DATE */}

                        <div className="rental-form-group">

                            <label htmlFor="dueDate">
                                Due Date
                            </label>

                            <input
                                id="dueDate"
                                type="date"
                                value={dueDate}
                                min={
                                    startDate || undefined
                                }
                                onChange={(event) =>
                                    setDueDate(
                                        event.target.value
                                    )
                                }
                            />

                        </div>


                        {/* NOTES */}

                        <div className="rental-form-group full-width">

                            <label htmlFor="notes">
                                Notes
                            </label>

                            <textarea
                                id="notes"
                                value={notes}
                                onChange={(event) =>
                                    setNotes(
                                        event.target.value
                                    )
                                }
                                placeholder="Enter any notes about this rental"
                                rows="4"
                            />

                        </div>


                    </div>

                </section>


                {/* =================================
                    MESSAGES
                ================================= */}

                {error && (

                    <div className="rental-form-error">

                        {error}

                    </div>

                )}


                {success && (

                    <div className="rental-form-success">

                        {success}

                    </div>

                )}


                {/* =================================
                    ACTIONS
                ================================= */}

                <div className="rental-form-actions">


                    <button
                        type="button"
                        className="rental-secondary-button"
                        onClick={handleClear}
                        disabled={saving}
                    >
                        Clear Form
                    </button>


                    <div className="rental-right-actions">

                        {onBack && (

                            <button
                                type="button"
                                className="rental-secondary-button"
                                onClick={onBack}
                                disabled={saving}
                            >
                                Back
                            </button>

                        )}


                        <button
                            type="button"
                            className="rental-primary-button"
                            onClick={handleContinue}
                            disabled={saving}
                        >
                            Continue to Equipment
                        </button>

                    </div>

                </div>


            </main>

        </div>
    );
}


export default CreateRental;