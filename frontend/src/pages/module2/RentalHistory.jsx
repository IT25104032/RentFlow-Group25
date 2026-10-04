import React, { useEffect, useState } from "react";
import { getAllCustomers } from "../../services/module2/customerService";
import { getCustomerRentalHistory } from "../../services/module2/rentalService";

import "./RentalHistory.css";


const COMPANY_ID = 1000;


function RentalHistory() {

    const [renters, setRenters] = useState([]);

    const [selectedRenter, setSelectedRenter] =
        useState(null);

    const [rentalHistory, setRentalHistory] =
        useState([]);

    const [searchText, setSearchText] =
        useState("");

    const [loadingRenters, setLoadingRenters] =
        useState(false);

    const [loadingHistory, setLoadingHistory] =
        useState(false);

    const [error, setError] =
        useState("");

    const [historyError, setHistoryError] =
        useState("");

    const [selectedRental, setSelectedRental] =
        useState(null);

    /*
     * Load all registered renters.
     */
    useEffect(() => {

        async function loadRenters() {

            setLoadingRenters(true);
            setError("");

            try {

                const data =
                    await getAllCustomers(COMPANY_ID);

                setRenters(data);

            } catch (err) {

                console.error(err);

                setError(
                    err.message ||
                    "Failed to load renters."
                );

            } finally {

                setLoadingRenters(false);

            }
        }

        loadRenters();

    }, []);


    /*
     * Filter renters using name,
     * phone or customer ID.
     */
    const filteredRenters =
        renters.filter(renter => {

            const search =
                searchText
                    .trim()
                    .toLowerCase();

            if (!search) {
                return true;
            }

            return (
                String(renter.customerId || "")
                    .toLowerCase()
                    .includes(search)
                ||
                (renter.customerName || "")
                    .toLowerCase()
                    .includes(search)
                ||
                (renter.phone || "")
                    .toLowerCase()
                    .includes(search)
            );

        });


    /*
     * Select renter and load
     * their rental history.
     */
    async function handleSelectRenter(renter) {

        setSelectedRenter(renter);

        setRentalHistory([]);

        setHistoryError("");

        setLoadingHistory(true);

        try {

            const history =
                await getCustomerRentalHistory(
                    renter.customerId,
                    COMPANY_ID
                );

            setRentalHistory(history);

        } catch (err) {

            console.error(err);

            setHistoryError(
                err.message ||
                "Failed to load rental history."
            );

        } finally {

            setLoadingHistory(false);

        }
    }


    /*
     * Clear the selected renter.
     */
    function handleClearSelection() {

        setSelectedRenter(null);

        setRentalHistory([]);

        setHistoryError("");

    }


    /*
     * Format date values.
     */
    function formatDate(date) {

        if (!date) {
            return "-";
        }

        return new Date(date).toLocaleDateString(
            "en-GB"
        );

    }


    /*
     * Format rental status.
     */
    function getStatusClass(status) {

        if (!status) {
            return "history-status";
        }

        return (
            "history-status " +
            status.toLowerCase()
        );

    }


    return (

        <div className="rental-history-page">


            {/* PAGE HEADER */}

            <div className="history-page-header">

                <h1>
                    Rental History
                </h1>

                <p>
                    View previous and current
                    rentals for registered renters.
                </p>

            </div>


            {/* RENTER SELECTION */}

            <div className="history-card">

                <div className="history-card-header">

                    <div>

                        <h2>
                            Select Renter
                        </h2>

                        <p>
                            Search for a renter to
                            view their rental history.
                        </p>

                    </div>

                </div>


                <div className="history-search">

                    <input
                        type="text"
                        value={searchText}
                        onChange={event =>
                            setSearchText(
                                event.target.value
                            )
                        }
                        placeholder="Search by renter name, phone or customer ID"
                    />

                </div>


                {loadingRenters && (

                    <div className="history-message">
                        Loading renters...
                    </div>

                )}


                {error && (

                    <div className="history-error">
                        {error}
                    </div>

                )}


                {!loadingRenters &&
                    !error &&
                    filteredRenters.length > 0 && (

                        <div className="history-renter-table-wrapper">

                            <table className="history-renter-table">

                                <thead>

                                <tr>

                                    <th>Customer ID</th>

                                    <th>Renter Name</th>

                                    <th>Phone</th>

                                    <th>Type</th>

                                    <th>Actions</th>

                                </tr>

                                </thead>


                                <tbody>

                                {filteredRenters.map(
                                    renter => (

                                        <tr
                                            key={
                                                renter.customerId
                                            }
                                        >

                                            <td>
                                                #{renter.customerId}
                                            </td>

                                            <td>
                                                <strong>
                                                    {
                                                        renter.customerName
                                                    }
                                                </strong>
                                            </td>

                                            <td>
                                                {
                                                    renter.phone ||
                                                    "-"
                                                }
                                            </td>

                                            <td>
                                                {
                                                    renter.customerType ||
                                                    "-"
                                                }
                                            </td>

                                            <td>

                                                <button
                                                    type="button"
                                                    className="history-select-button"
                                                    onClick={() =>
                                                        handleSelectRenter(
                                                            renter
                                                        )
                                                    }
                                                >
                                                    View History
                                                </button>

                                            </td>

                                        </tr>

                                    ))}

                                </tbody>

                            </table>

                        </div>

                    )}


                {!loadingRenters &&
                    !error &&
                    filteredRenters.length === 0 && (

                        <div className="history-empty">

                            No renters found.

                        </div>

                    )}

            </div>


            {/* SELECTED RENTER */}

            {selectedRenter && (

                <div className="history-card">

                    <div className="history-card-header selected-renter-header">

                        <div>

                            <h2>
                                Rental History
                            </h2>

                            <p>
                                Rental records for{" "}
                                <strong>
                                    {
                                        selectedRenter.customerName
                                    }
                                </strong>
                            </p>

                        </div>


                        <button
                            type="button"
                            className="history-clear-button"
                            onClick={
                                handleClearSelection
                            }
                        >
                            Change Renter
                        </button>

                    </div>


                    {/* RENTER SUMMARY */}

                    <div className="history-renter-summary">

                        <div className="history-summary-item">

                            <span>
                                Customer ID
                            </span>

                            <strong>
                                #
                                {
                                    selectedRenter.customerId
                                }
                            </strong>

                        </div>


                        <div className="history-summary-item">

                            <span>
                                Renter
                            </span>

                            <strong>
                                {
                                    selectedRenter.customerName
                                }
                            </strong>

                        </div>


                        <div className="history-summary-item">

                            <span>
                                Phone
                            </span>

                            <strong>
                                {
                                    selectedRenter.phone ||
                                    "-"
                                }
                            </strong>

                        </div>


                        <div className="history-summary-item">

                            <span>
                                Total Rentals
                            </span>

                            <strong>
                                {rentalHistory.length}
                            </strong>

                        </div>

                    </div>


                    {/* LOADING */}

                    {loadingHistory && (

                        <div className="history-message">
                            Loading rental history...
                        </div>

                    )}


                    {/* ERROR */}

                    {historyError && (

                        <div className="history-error">
                            {historyError}
                        </div>

                    )}


                    {/* HISTORY TABLE */}

                    {!loadingHistory &&
                        !historyError &&
                        rentalHistory.length > 0 && (

                            <div className="history-table-wrapper">

                                <table className="rental-history-table">

                                    <thead>

                                    <tr>

                                        <th>
                                            Rental ID
                                        </th>

                                        <th>
                                            Rental Date
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
                                            Equipment
                                        </th>

                                        <th>
                                            Action
                                        </th>

                                    </tr>

                                    </thead>


                                    <tbody>

                                    {rentalHistory.map(record => {

                                        const rental =
                                            record.rental;

                                        const items =
                                            record.rentalItems || [];


                                        return (

                                            <tr
                                                key={rental.rentalId}
                                            >

                                                <td>

                                                    <strong>
                                                        #
                                                        {
                                                            rental.rentalId
                                                        }
                                                    </strong>

                                                </td>


                                                <td>
                                                    {
                                                        formatDate(
                                                            rental.rentalDate
                                                        )
                                                    }
                                                </td>


                                                <td>
                                                    {
                                                        formatDate(
                                                            rental.startDate
                                                        )
                                                    }
                                                </td>


                                                <td>
                                                    {
                                                        formatDate(
                                                            rental.dueDate
                                                        )
                                                    }
                                                </td>


                                                <td>

                        <span
                            className={
                                getStatusClass(
                                    rental.rentalStatus
                                )
                            }
                        >
                            {
                                rental.rentalStatus
                            }
                        </span>

                                                </td>


                                                <td>

                                                    {
                                                        items.length > 0
                                                            ? `${items.length} item${
                                                                items.length !== 1
                                                                    ? "s"
                                                                    : ""
                                                            }`
                                                            : "No items"
                                                    }

                                                </td>


                                                {/* IMPORTANT:
                        This button is INSIDE
                        the map, so it knows
                        which rental was clicked.
                    */}

                                                <td>

                                                    <button
                                                        type="button"
                                                        className="history-details-button"
                                                        onClick={() =>
                                                            setSelectedRental(
                                                                record
                                                            )
                                                        }
                                                    >
                                                        View Details
                                                    </button>

                                                </td>

                                            </tr>

                                        );

                                    })}

                                    </tbody>

                                </table>

                            </div>

                        )}


                    {!loadingHistory &&
                        !historyError &&
                        rentalHistory.length === 0 && (

                            <div className="history-empty">

                                No rental history found
                                for this renter.

                            </div>

                        )}

                </div>

            )}
            {selectedRental && (

                <div
                    className="rental-details-overlay"
                    onClick={() => setSelectedRental(null)}
                >

                    <div
                        className="rental-details-modal"
                        onClick={event =>
                            event.stopPropagation()
                        }
                    >

                        <div className="rental-details-header">

                            <div>

                                <h2>
                                    Rental #
                                    {
                                        selectedRental.rental.rentalId
                                    }
                                </h2>

                                <p>
                                    Detailed rental information
                                </p>

                            </div>

                            <button
                                type="button"
                                className="rental-details-close"
                                onClick={() =>
                                    setSelectedRental(null)
                                }
                            >
                                ×
                            </button>

                        </div>


                        {/* Rental information */}

                        <div className="rental-details-section">

                            <h3>
                                Rental Information
                            </h3>

                            <div className="rental-details-grid">

                                <div>
                                    <span>Rental Date</span>
                                    <strong>
                                        {
                                            formatDate(
                                                selectedRental.rental.rentalDate
                                            )
                                        }
                                    </strong>
                                </div>

                                <div>
                                    <span>Start Date</span>
                                    <strong>
                                        {
                                            formatDate(
                                                selectedRental.rental.startDate
                                            )
                                        }
                                    </strong>
                                </div>

                                <div>
                                    <span>Due Date</span>
                                    <strong>
                                        {
                                            formatDate(
                                                selectedRental.rental.dueDate
                                            )
                                        }
                                    </strong>
                                </div>

                                <div>
                                    <span>Status</span>

                                    <strong>
                            <span
                                className={getStatusClass(
                                    selectedRental.rental.rentalStatus
                                )}
                            >
                                {
                                    selectedRental.rental.rentalStatus
                                }
                            </span>
                                    </strong>
                                </div>

                            </div>

                        </div>


                        {/* Equipment */}

                        <div className="rental-details-section">

                            <h3>
                                Rented Equipment
                            </h3>

                            <div className="rental-details-table-wrapper">

                                <table className="rental-details-table">

                                    <thead>

                                    <tr>

                                        <th>
                                            Equipment ID
                                        </th>

                                        <th>
                                            Quantity
                                        </th>

                                        <th>
                                            Rate / Unit
                                        </th>

                                        <th>
                                            Rate Period
                                        </th>

                                        <th>
                                            Deposit / Unit
                                        </th>

                                        <th>
                                            Line Deposit
                                        </th>

                                    </tr>

                                    </thead>


                                    <tbody>

                                    {(
                                        selectedRental.rentalItems ||
                                        []
                                    ).map(item => (

                                        <tr
                                            key={
                                                item.rentalItemId
                                            }
                                        >

                                            <td>
                                                #
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
                                                Rs.{" "}
                                                {
                                                    Number(
                                                        item.ratePerUnit
                                                    ).toLocaleString()
                                                }
                                            </td>

                                            <td>
                                                {
                                                    item.ratePeriod
                                                }
                                            </td>

                                            <td>
                                                Rs.{" "}
                                                {
                                                    Number(
                                                        item.depositPerUnit
                                                    ).toLocaleString()
                                                }
                                            </td>

                                            <td>
                                                Rs.{" "}
                                                {
                                                    Number(
                                                        item.lineDeposit
                                                    ).toLocaleString()
                                                }
                                            </td>

                                        </tr>

                                    ))}

                                    </tbody>

                                </table>

                            </div>

                        </div>


                        {/* Notes */}

                        <div className="rental-details-section">

                            <h3>
                                Notes
                            </h3>

                            <p className="rental-details-notes">

                                {
                                    selectedRental.rental.notes ||
                                    "No notes recorded."
                                }

                            </p>

                        </div>


                        <div className="rental-details-footer">

                            <button
                                type="button"
                                onClick={() =>
                                    setSelectedRental(null)
                                }
                            >
                                Close
                            </button>

                        </div>

                    </div>

                </div>

            )}
        </div>

    );

}


export default RentalHistory;