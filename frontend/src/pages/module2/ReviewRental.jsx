import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import RentalStepper from "../../components/module2/RentalStepper";
import "./ReviewRental.css";


function ReviewRental() {

    const location = useLocation();
    const navigate = useNavigate();


    const [confirming, setConfirming] =
        useState(false);

    const [error, setError] =
        useState("");

    const [success, setSuccess] =
        useState("");


    let savedDraft = {};

    try {
        savedDraft = JSON.parse(
            sessionStorage.getItem("rentFlowRentalDraft") || "{}"
        );
    } catch {
        savedDraft = {};
    }

    const draft = {
        ...savedDraft,
        ...(location.state || {})
    };

    const selectedRenter = draft.selectedRenter || null;
    const rentalDetails = draft.rentalDetails || null;
    const selectedEquipment = draft.selectedEquipment || [];
    const totalDeposit = draft.totalDeposit || 0;



    // Calculate the rental duration in days.
    const start = rentalDetails?.startDate
        ? new Date(`${rentalDetails.startDate}T00:00:00`)
        : null;

    const due = rentalDetails?.dueDate
        ? new Date(`${rentalDetails.dueDate}T00:00:00`)
        : null;

    const rentalDays =
        start && due && !Number.isNaN(start.getTime()) &&
        !Number.isNaN(due.getTime())
            ? Math.max(
                1,
                Math.round((due - start) / (1000 * 60 * 60 * 24))
            )
            : 1;

    // Total rental charges, excluding security deposits.
    const totalRentalAmount = selectedEquipment.reduce(
        (total, item) =>
            total +
            Number(item.quantity || 0) *
            Number(item.rentalRate ?? item.ratePerUnit ?? 0) *
            rentalDays,
        0
    );



    /*
     * Go back to Equipment Selection while
     * keeping the current selections.
     */
    const handleBack = () => {

        sessionStorage.setItem(
            "rentFlowRentalDraft",
            JSON.stringify({
                ...draft,
                selectedRenter,
                rentalDetails,
                selectedEquipment,
                totalDeposit
            })
        );

        navigate(
            "/rentals/equipment",
            {
                state: {
                    selectedRenter,
                    rentalDetails,
                    selectedEquipment,
                    totalDeposit
                }
            }
        );
    };


    /*
     * Confirm the complete rental.
     *
     * Sends the rental details and selected
     * equipment to the Spring Boot backend.
     */
    async function handleConfirmRental() {

        if (!selectedRenter || !rentalDetails) {

            setError(
                "Rental information is incomplete."
            );

            return;
        }


        if (
            !selectedEquipment ||
            selectedEquipment.length === 0
        ) {

            setError(
                "Please select at least one equipment item."
            );

            return;
        }


        setConfirming(true);
        setError("");
        setSuccess("");


        try {

            /*
             * Convert the selected equipment
             * into the structure expected by
             * RentalConfirmationRequest.
             */
            const rentalItems =
                selectedEquipment.map(
                    (item) => ({

                        equipmentId:
                        item.equipmentId,

                        quantity:
                        item.quantity,

                        ratePerUnit:
                            item.rentalRate ??
                            item.ratePerUnit,

                        ratePeriod:
                            item.ratePeriod ??
                            item.rentalPeriod,

                        /*
                         * IMPORTANT:
                         * EquipmentSelection stores
                         * the deposit as
                         * securityDepositPerUnit.
                         *
                         * The backend expects
                         * depositPerUnit.
                         */
                        depositPerUnit:
                            item.securityDepositPerUnit ??
                            item.depositPerUnit
                    })
                );


            /*
             * Build the complete confirmation
             * request.
             */
            const requestBody = {

                rental: {

                    companyId:
                    rentalDetails.companyId,

                    customerId:
                    rentalDetails.customerId,

                    createdBy:
                    rentalDetails.createdBy,

                    startDate:
                    rentalDetails.startDate,

                    dueDate:
                    rentalDetails.dueDate,

                    notes:
                    rentalDetails.notes
                },


                rentalItems:
                rentalItems
            };


            /*
             * Send the confirmation request
             * to Spring Boot.
             */
            const response =
                await fetch(
                    "http://localhost:8081/api/rentals/confirm",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify(
                                requestBody
                            )
                    }
                );


            /*
             * Check whether the backend
             * returned a successful response.
             */
            if (!response.ok) {

                const errorText =
                    await response.text();

                throw new Error(
                    errorText ||
                    "Failed to confirm rental."
                );
            }


            /*
             * Convert the backend JSON response
             * into a JavaScript object.
             */
            const result =
                await response.json();


            /*
             * Show the generated rental ID.
             */
            setSuccess(
                `Rental ${result.rental.rentalId} confirmed successfully.`
            );
            sessionStorage.removeItem("rentFlowRentalDraft");

            setTimeout(() => {
                navigate("/renters");
            }, 2500);

        } catch (error) {

            console.error(
                "Rental confirmation failed:",
                error
            );


            setError(
                error.message ||
                "Failed to confirm rental."
            );

        } finally {

            /*
             * Re-enable the Confirm button
             * after the request finishes.
             */
            setConfirming(false);
        }
    }


    return (

        <div className="review-rental-page">


            {/* Page heading */}

            <div className="review-page-header">

                <h1>
                    Create Rental
                </h1>

                <p>
                    Review the rental details
                    before confirmation.
                </p>

            </div>


            <RentalStepper
                activeStep={4}
            />


            {/* Renter Information */}

            <div className="review-card">

                <div className="review-card-header">

                    <div>

                        <h2>
                            Renter Information
                        </h2>

                        <p>
                            Confirm the renter
                            for this rental.
                        </p>

                    </div>

                </div>


                {selectedRenter ? (

                    <div className="review-info-grid">

                        <div className="review-info-item">

                            <span>
                                Renter Name
                            </span>

                            <strong>
                                {
                                    selectedRenter.customerName
                                }
                            </strong>

                        </div>


                        <div className="review-info-item">

                            <span>
                                Phone
                            </span>

                            <strong>
                                {
                                    selectedRenter.phone
                                }
                            </strong>

                        </div>


                        <div className="review-info-item">

                            <span>
                                Email
                            </span>

                            <strong>
                                {
                                    selectedRenter.email ||
                                    "Not provided"
                                }
                            </strong>

                        </div>


                        <div className="review-info-item">

                            <span>
                                Customer Type
                            </span>

                            <strong>
                                {
                                    selectedRenter.customerType ||
                                    "Not specified"
                                }
                            </strong>

                        </div>

                    </div>

                ) : (

                    <div className="review-empty">
                        Renter information was not found.
                    </div>

                )}

            </div>


            {/* Rental Details */}

            <div className="review-card">

                <div className="review-card-header">

                    <div>

                        <h2>
                            Rental Details
                        </h2>

                        <p>
                            Confirm the rental period
                            and notes.
                        </p>

                    </div>

                </div>


                <div className="review-info-grid">

                    <div className="review-info-item">

                        <span>
                            Start Date
                        </span>

                        <strong>
                            {
                                rentalDetails?.startDate ||
                                "Not provided"
                            }
                        </strong>

                    </div>


                    <div className="review-info-item">

                        <span>
                            Due Date
                        </span>

                        <strong>
                            {
                                rentalDetails?.dueDate ||
                                "Not provided"
                            }
                        </strong>

                    </div>


                    <div className="review-info-item">

                        <span>
                            Rental Status
                        </span>

                        <strong className="status-badge">

                            {
                                rentalDetails?.rentalStatus ||
                                "DRAFT"
                            }

                        </strong>

                    </div>


                    <div className="review-info-item">

                        <span>
                            Notes
                        </span>

                        <strong>
                            {
                                rentalDetails?.notes ||
                                "No notes"
                            }
                        </strong>

                    </div>

                </div>

            </div>


            {/* Equipment */}

            <div className="review-card">

                <div className="review-card-header">

                    <div>

                        <h2>
                            Selected Equipment
                        </h2>

                        <p>
                            Review the equipment and
                            requested quantities.
                        </p>

                    </div>

                </div>


                {selectedEquipment.length > 0 ? (

                    <div className="review-equipment-table-wrapper">

                        <table className="review-equipment-table">

                            <thead>
                            <tr>
                                <th>Equipment</th>
                                <th>Code</th>
                                <th>Quantity</th>
                                <th>Rate</th>
                                <th>Deposit</th>
                            </tr>
                            </thead>

                            <tbody>

                            {selectedEquipment.map(
                                (item) => (

                                    <tr
                                        key={
                                            item.equipmentId
                                        }
                                    >

                                        <td>

                                            <strong>
                                                {
                                                    item.itemName
                                                }
                                            </strong>

                                            <span>
                                                    {
                                                        item.categoryName
                                                    }
                                                </span>

                                        </td>
                                        <td>
                                            {item.itemCode}
                                        </td>
                                        <td>
                                            {item.quantity}
                                        </td>
                                        <td>
                                            Rs.{" "}

                                            {
                                                item.rentalRate
                                                    .toLocaleString()
                                            }

                                            /

                                            {
                                                item.ratePeriod
                                                    .toLowerCase()
                                            }

                                        </td>


                                        <td>

                                            Rs.{" "}

                                            {

                                                (
                                                    item.quantity *
                                                    (
                                                        item.securityDepositPerUnit ??
                                                        item.depositPerUnit ??
                                                        0
                                                    )
                                                ).toLocaleString()

                                            }

                                        </td>

                                    </tr>

                                )
                            )}

                            </tbody>

                        </table>

                    </div>

                ) : (

                    <div className="review-empty">
                        No equipment has been selected.
                    </div>

                )}

            </div>


            {/* Financial Summary */}

            <div className="review-card">

                <div className="review-card-header">

                    <div>

                        <h2>
                            Rental Summary
                        </h2>

                        <p>
                            Summary of the selected
                            equipment and refundable deposit.
                        </p>

                    </div>

                </div>


                <div className="review-summary">


                    <div className="summary-row">
                        <span>
                            Total Rental Amount (Excluding Security Deposit)
                        </span>

                        <strong>
                            Rs. {totalRentalAmount.toLocaleString()}
                        </strong>
                    </div>

                    <div className="summary-row deposit-row">

                        <span>
                            Total Security Deposit
                        </span>

                        <strong>

                            Rs.{" "}

                            {
                                totalDeposit
                                    .toLocaleString()
                            }

                        </strong>

                    </div>

                </div>

            </div>


            {/* Success / Error Messages */}

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

            <div className="review-actions">

                <button
                    type="button"
                    className="review-back-button"
                    onClick={handleBack}
                    disabled={confirming}
                >
                    Back
                </button>


                <button
                    type="button"
                    className="review-confirm-button"
                    onClick={handleConfirmRental}
                    disabled={confirming}
                >

                    {confirming
                        ? "Confirming..."
                        : "Confirm Rental"}

                </button>

            </div>


        </div>
    );
}


export default ReviewRental;