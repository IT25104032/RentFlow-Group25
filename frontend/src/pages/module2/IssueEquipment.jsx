import { useEffect, useState } from "react";

import {
    getActiveRentalsForIssue,
    getRentalItemsForIssue,
    issueRentalItem
} from "../../services/module2/equipmentIssueService";

import "./IssueEquipment.css";

const COMPANY_ID = 1000;

function IssueEquipment() {

    const [rentals, setRentals] = useState([]);
    const [selectedRental, setSelectedRental] = useState(null);
    const [rentalItems, setRentalItems] = useState([]);

    const [searchTerm, setSearchTerm] = useState("");

    const [loadingRentals, setLoadingRentals] = useState(true);
    const [loadingItems, setLoadingItems] = useState(false);

    const [issuingItemId, setIssuingItemId] =
        useState(null);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");


    /*
     * Load active rentals when page opens.
     */
    useEffect(() => {
        loadActiveRentals();
    }, []);


    async function loadActiveRentals() {

        try {

            setLoadingRentals(true);
            setError("");

            const data =
                await getActiveRentalsForIssue(
                    COMPANY_ID
                );

            setRentals(data || []);

        } catch (err) {

            console.error(err);

            setError(
                "Unable to load active rentals."
            );

        } finally {

            setLoadingRentals(false);
        }
    }


    /*
     * Select a rental and load its items.
     */
    async function handleSelectRental(rental) {

        try {

            setSelectedRental(rental);
            setRentalItems([]);

            setLoadingItems(true);

            setError("");
            setSuccess("");

            const items =
                await getRentalItemsForIssue(
                    rental.rentalId
                );

            setRentalItems(items || []);

        } catch (err) {

            console.error(err);

            setError(
                "Unable to load the equipment for this rental."
            );

        } finally {

            setLoadingItems(false);
        }
    }


    /*
     * Issue one rental item.
     */
    async function handleIssueItem(item) {

        if (!selectedRental) {
            return;
        }

        const confirmed = window.confirm(
            `Issue ${item.quantity} unit(s) of equipment #${item.equipmentId} for rental #${selectedRental.rentalId}?`
        );

        if (!confirmed) {
            return;
        }

        try {

            setIssuingItemId(
                item.rentalItemId
            );

            setError("");
            setSuccess("");

            const updatedItem =
                await issueRentalItem(
                    item.rentalItemId,
                    selectedRental.rentalId
                );

            /*
             * Update the item immediately
             * using the backend response.
             */
            setRentalItems(previousItems =>
                previousItems.map(existingItem =>
                    existingItem.rentalItemId ===
                    updatedItem.rentalItemId
                        ? updatedItem
                        : existingItem
                )
            );

            setSuccess(
                `Rental item #${item.rentalItemId} was successfully issued.`
            );

        } catch (err) {

            console.error(err);

            setError(
                "Unable to issue this equipment."
            );

        } finally {

            setIssuingItemId(null);
        }
    }


    /*
     * Search active rentals.
     */
    const filteredRentals =
        rentals.filter(rental => {

            const search =
                searchTerm
                    .trim()
                    .toLowerCase();

            if (!search) {
                return true;
            }

            return (
                String(rental.rentalId)
                    .toLowerCase()
                    .includes(search)
                ||
                String(rental.customerId)
                    .toLowerCase()
                    .includes(search)
                ||
                String(rental.rentalStatus)
                    .toLowerCase()
                    .includes(search)
            );
        });


    /*
     * Format dates.
     */
    function formatDate(date) {

        if (!date) {
            return "-";
        }

        return new Date(date).toLocaleDateString(
            "en-GB",
            {
                day: "2-digit",
                month: "short",
                year: "numeric"
            }
        );
    }


    return (
        <div className="issue-equipment-page">

            {/* =========================================
                PAGE HEADER
            ========================================= */}

            <div className="module2-page-header">

                <div>

                    <h1>
                        Issue Equipment
                    </h1>

                    <p>
                        Issue equipment for an active rental
                        and record the issued items.
                    </p>

                </div>

            </div>


            {/* =========================================
                ALERTS
            ========================================= */}

            {error && (
                <div className="module2-alert module2-alert-error">
                    {error}
                </div>
            )}

            {success && (
                <div className="module2-alert module2-alert-success">
                    {success}
                </div>
            )}


            {/* =========================================
                ACTIVE RENTALS
            ========================================= */}

            <div className="module2-card">

                <div className="module2-card-header">

                    <div>

                        <h2>
                            Active Rentals
                        </h2>

                        <p>
                            Select a rental to view
                            its equipment.
                        </p>

                    </div>

                </div>


                <div className="issue-equipment-search">

                    <input
                        type="text"
                        placeholder="Search by rental ID, customer ID or status..."
                        value={searchTerm}
                        onChange={event =>
                            setSearchTerm(
                                event.target.value
                            )
                        }
                    />

                </div>


                {loadingRentals ? (

                    <div className="module2-empty-state">
                        Loading active rentals...
                    </div>

                ) : filteredRentals.length === 0 ? (

                    <div className="module2-empty-state">
                        No active rentals found.
                    </div>

                ) : (

                    <div className="module2-table-wrapper">

                        <table className="module2-table">

                            <thead>

                            <tr>
                                <th>
                                    Rental ID
                                </th>

                                <th>
                                    Customer ID
                                </th>

                                <th>
                                    Start Date
                                </th>

                                <th>
                                    Due Date
                                </th>

                                <th>
                                    Status
                                </th>

                                <th>
                                    Action
                                </th>
                            </tr>

                            </thead>


                            <tbody>

                            {filteredRentals.map(
                                rental => (

                                    <tr
                                        key={
                                            rental.rentalId
                                        }
                                        className={
                                            selectedRental?.rentalId ===
                                            rental.rentalId
                                                ? "selected-row"
                                                : ""
                                        }
                                    >

                                        <td>
                                            #
                                            {
                                                rental.rentalId
                                            }
                                        </td>

                                        <td>
                                            {
                                                rental.customerId
                                            }
                                        </td>

                                        <td>
                                            {formatDate(
                                                rental.startDate
                                            )}
                                        </td>

                                        <td>
                                            {formatDate(
                                                rental.dueDate
                                            )}
                                        </td>

                                        <td>

                                                <span className="rental-status-badge">
                                                    {
                                                        rental.rentalStatus
                                                    }
                                                </span>

                                        </td>

                                        <td>

                                            <button
                                                type="button"
                                                className="module2-secondary-button"
                                                onClick={() =>
                                                    handleSelectRental(
                                                        rental
                                                    )
                                                }
                                            >
                                                Select
                                            </button>

                                        </td>

                                    </tr>

                                )
                            )}

                            </tbody>

                        </table>

                    </div>

                )}

            </div>


            {/* =========================================
                SELECTED RENTAL
            ========================================= */}

            {selectedRental && (

                <div className="module2-card">

                    <div className="module2-card-header">

                        <div>

                            <h2>
                                Rental #
                                {
                                    selectedRental.rentalId
                                }
                            </h2>

                            <p>
                                Equipment assigned to this
                                rental.
                            </p>

                        </div>

                    </div>


                    <div className="issue-rental-summary">

                        <div>

                            <span>
                                Customer ID
                            </span>

                            <strong>
                                {
                                    selectedRental.customerId
                                }
                            </strong>

                        </div>


                        <div>

                            <span>
                                Start Date
                            </span>

                            <strong>
                                {formatDate(
                                    selectedRental.startDate
                                )}
                            </strong>

                        </div>


                        <div>

                            <span>
                                Due Date
                            </span>

                            <strong>
                                {formatDate(
                                    selectedRental.dueDate
                                )}
                            </strong>

                        </div>


                        <div>

                            <span>
                                Status
                            </span>

                            <strong>
                                {
                                    selectedRental.rentalStatus
                                }
                            </strong>

                        </div>

                    </div>


                    {/* =================================
                        RENTAL ITEMS
                    ================================= */}

                    {loadingItems ? (

                        <div className="module2-empty-state">
                            Loading rental equipment...
                        </div>

                    ) : rentalItems.length === 0 ? (

                        <div className="module2-empty-state">
                            No equipment items found for
                            this rental.
                        </div>

                    ) : (

                        <div className="module2-table-wrapper">

                            <table className="module2-table">

                                <thead>

                                <tr>

                                    <th>
                                        Rental Item
                                    </th>

                                    <th>
                                        Equipment ID
                                    </th>

                                    <th>
                                        Quantity
                                    </th>

                                    <th>
                                        Status
                                    </th>

                                    <th>
                                        Issued At
                                    </th>

                                    <th>
                                        Action
                                    </th>

                                </tr>

                                </thead>


                                <tbody>

                                {rentalItems.map(
                                    item => {

                                        const isIssued =
                                            item.itemStatus === "ISSUED"
                                            || (
                                                item.itemStatus === "PARTIALLY_RETURNED"
                                                && item.issuedAt
                                            );

                                        const isSelected =
                                            item.itemStatus ===
                                            "SELECTED";

                                        const isIssuing =
                                            issuingItemId ===
                                            item.rentalItemId;

                                        return (

                                            <tr
                                                key={
                                                    item.rentalItemId
                                                }
                                            >

                                                <td>
                                                    #
                                                    {
                                                        item.rentalItemId
                                                    }
                                                </td>

                                                <td>
                                                    {
                                                        item.equipmentId
                                                    }
                                                </td>

                                                <td>
                                                    {
                                                        item.quantity
                                                    }
                                                </td>

                                                <td>

                                                        <span
                                                            className={
                                                                isIssued
                                                                    ? "equipment-status-badge issued"
                                                                    : "equipment-status-badge selected"
                                                            }
                                                        >
                                                            {
                                                                item.itemStatus
                                                            }
                                                        </span>

                                                </td>

                                                <td>
                                                    {item.issuedAt
                                                        ? formatDate(
                                                            item.issuedAt
                                                        )
                                                        : "-"
                                                    }
                                                </td>

                                                <td>

                                                    {isIssued ? (

                                                        <span className="issued-label">
                                                            ✓ Issued
                                                        </span>

                                                    ) : isSelected ? (

                                                        <button
                                                            type="button"
                                                            className="module2-primary-button"
                                                            disabled={isIssuing}
                                                            onClick={() =>
                                                                handleIssueItem(item)
                                                            }
                                                        >
                                                            {isIssuing
                                                                ? "Issuing..."
                                                                : "Issue Equipment"
                                                            }
                                                        </button>

                                                    ) : (

                                                        <span className="issued-label">
                                                            {item.itemStatus}
                                                        </span>

                                                    )}

                                                </td>

                                            </tr>

                                        );
                                    }
                                )}

                                </tbody>

                            </table>

                        </div>

                    )}

                </div>

            )}

        </div>
    );
}

export default IssueEquipment;