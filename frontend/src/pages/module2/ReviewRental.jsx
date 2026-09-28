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


    const selectedRenter =
        location.state?.selectedRenter || null;


    const rentalDetails =
        location.state?.rentalDetails || null;


    const selectedEquipment =
        location.state?.selectedEquipment || [];


    const totalDeposit =
        location.state?.totalDeposit || 0;


    /*
     * Calculate the estimated rental amount.
     *
     * At this stage the equipment rate is stored
     * per unit/per day. Later we can connect this
     * to the actual rental period calculation.
     */
    const totalDailyRentalRate =
        selectedEquipment.reduce(
            (total, item) =>
                total +
                (
                    item.quantity *
                    item.rentalRate
                ),
            0
        );


    /*
     * Go back to Equipment Selection while
     * keeping the current selections.
     */
    const handleBack = () => {

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
                `Rental #${result.rental.rentalId} confirmed successfully.`
            );

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

                                <th>
                                    Equipment
                                </th>

                                <th>
                                    Code
                                </th>

                                <th>
                                    Quantity
                                </th>

                                <th>
                                    Rate
                                </th>

                                <th>
                                    Deposit
                                </th>

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

                                            {
                                                item.itemCode
                                            }

                                        </td>


                                        <td>

                                            {
                                                item.quantity
                                            }

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
                            Estimated Daily Rental Rate
                        </span>

                        <strong>

                            Rs.{" "}

                            {
                                totalDailyRentalRate
                                    .toLocaleString()
                            }

                        </strong>

                    </div>


                    <div className="summary-row deposit-row">

                        <span>
                            Total Refundable Deposit
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