import { useState } from "react";
import { useNavigate } from "react-router-dom";

import {
    searchRentals,
    getRentalForReturn,
    processReturn,
    getReturnItems
} from "../../services/module4/module4Api.js";

import "../../styles/module4/module4.css";


function ProcessReturn() {

    const navigate = useNavigate();

    const [searchTerm, setSearchTerm] =
        useState("");

    const [searchResults, setSearchResults] =
        useState([]);

    const [selectedRental, setSelectedRental] =
        useState(null);

    const [returnItems, setReturnItems] =
        useState([]);

    const [generalNotes, setGeneralNotes] =
        useState("");

    const [error, setError] =
        useState("");

    const [searching, setSearching] =
        useState(false);

    const [loadingRental, setLoadingRental] =
        useState(false);

    const [saving, setSaving] =
        useState(false);

    // Temporary until authentication is connected.
    const CURRENT_USER_ID = 3;


    async function handleSearch() {

        setError("");
        setSelectedRental(null);
        setReturnItems([]);

        if (!searchTerm.trim()) {

            setSearchResults([]);
            return;
        }

        try {

            setSearching(true);

            const results =
                await searchRentals(
                    searchTerm.trim()
                );

            setSearchResults(results);

            if (results.length === 0) {

                setError(
                    "No matching rentals were found."
                );
            }

        } catch (err) {

            console.error(err);

            setSearchResults([]);

            setError(
                err.message ||
                "Unable to search rentals."
            );

        } finally {

            setSearching(false);
        }
    }


    async function handleSelectRental(
        rental
    ) {

        setError("");

        try {

            setLoadingRental(true);

            const rentalDetails =
                await getRentalForReturn(
                    rental.rentalId
                );

            setSelectedRental(
                rentalDetails
            );

            const preparedItems =
                rentalDetails.items.map(
                    (item) => ({
                        ...item,
                        selected: false,
                        quantityToReturn: 0,
                        conditionStatus: "GOOD",
                        inspectionNotes: ""
                    })
                );

            setReturnItems(
                preparedItems
            );

            setSearchResults([]);
            setGeneralNotes("");

        } catch (err) {

            console.error(err);

            setError(
                err.message ||
                "Unable to load rental details."
            );

        } finally {

            setLoadingRental(false);
        }
    }


    function getOutstandingQuantity(
        item
    ) {

        return item.remainingQuantity;
    }


    function handleItemSelection(
        rentalItemId
    ) {

        setReturnItems(
            (previousItems) =>
                previousItems.map(
                    (item) => {

                        if (
                            item.rentalItemId ===
                            rentalItemId
                        ) {

                            const selected =
                                !item.selected;

                            return {
                                ...item,
                                selected,
                                quantityToReturn:
                                    selected
                                        ? getOutstandingQuantity(
                                            item
                                        )
                                        : 0,
                                conditionStatus:
                                    selected
                                        ? item.conditionStatus
                                        : "GOOD",
                                inspectionNotes:
                                    selected
                                        ? item.inspectionNotes
                                        : ""
                            };
                        }

                        return item;
                    }
                )
        );
    }


    function handleQuantityChange(
        rentalItemId,
        quantity
    ) {

        setReturnItems(
            (previousItems) =>
                previousItems.map(
                    (item) =>
                        item.rentalItemId ===
                        rentalItemId
                            ? {
                                ...item,
                                quantityToReturn:
                                    Number(quantity)
                            }
                            : item
                )
        );
    }


    function handleConditionChange(
        rentalItemId,
        condition
    ) {

        setReturnItems(
            (previousItems) =>
                previousItems.map(
                    (item) =>
                        item.rentalItemId ===
                        rentalItemId
                            ? {
                                ...item,
                                conditionStatus:
                                condition
                            }
                            : item
                )
        );
    }


    function handleInspectionNotes(
        rentalItemId,
        notes
    ) {

        setReturnItems(
            (previousItems) =>
                previousItems.map(
                    (item) =>
                        item.rentalItemId ===
                        rentalItemId
                            ? {
                                ...item,
                                inspectionNotes:
                                notes
                            }
                            : item
                )
        );
    }


    function calculateReturnType() {

        if (!selectedRental) {
            return "";
        }

        let totalOutstanding = 0;
        let totalReturning = 0;

        returnItems.forEach(
            (item) => {

                totalOutstanding +=
                    getOutstandingQuantity(
                        item
                    );

                if (item.selected) {

                    totalReturning +=
                        Number(
                            item.quantityToReturn
                        );
                }
            }
        );

        if (
            totalReturning > 0 &&
            totalReturning ===
            totalOutstanding
        ) {

            return "FULL";
        }

        return "PARTIAL";
    }


    function validateReturn() {

        const selectedItems =
            returnItems.filter(
                (item) => item.selected
            );

        if (selectedItems.length === 0) {

            setError(
                "Please select at least one item."
            );

            return false;
        }

        for (const item of selectedItems) {

            const outstanding =
                getOutstandingQuantity(
                    item
                );

            if (
                item.quantityToReturn <= 0
            ) {

                setError(
                    `Return quantity for ${item.equipmentName} must be greater than zero.`
                );

                return false;
            }

            if (
                item.quantityToReturn >
                outstanding
            ) {

                setError(
                    `Cannot return ${item.quantityToReturn} ${item.equipmentName}. Only ${outstanding} outstanding.`
                );

                return false;
            }
        }

        return true;
    }


    async function handleProcessReturn() {

        setError("");

        if (!validateReturn()) {
            return;
        }

        try {

            setSaving(true);

            const selectedItems =
                returnItems.filter(
                    (item) => item.selected
                );

            const requestData = {

                rentalId:
                selectedRental.rentalId,

                processedBy:
                CURRENT_USER_ID,

                notes:
                generalNotes,

                items:
                    selectedItems.map(
                        (item) => ({
                            rentalItemId:
                            item.rentalItemId,
                            quantityReturned:
                                Number(
                                    item.quantityToReturn
                                ),
                            conditionStatus:
                            item.conditionStatus,
                            inspectionNotes:
                            item.inspectionNotes
                        })
                    )
            };

            const result =
                await processReturn(
                    requestData
                );

            const returnId =
                result.returnId;

            const damagedReturnItemIds =
                result.damagedReturnItemIds ||
                [];

            if (
                damagedReturnItemIds.length > 0
            ) {

                const savedItems =
                    await getReturnItems(
                        returnId
                    );

                const damagedItems =
                    savedItems.filter(
                        (item) =>
                            damagedReturnItemIds
                                .includes(
                                    item.returnItemId
                                )
                    );

                navigate(
                    `/returns/${returnId}/damages`,
                    {
                        state: {
                            damagedItems
                        }
                    }
                );

            } else {

                navigate(
                    `/returns/${returnId}`
                );
            }

        } catch (err) {

            console.error(err);

            setError(
                err.message ||
                "Unable to process return."
            );

        } finally {

            setSaving(false);
        }
    }


    return (

        <div className="module-page">

            <div className="page-header">

                <div>

                    <h1>
                        Process Rental Return
                    </h1>

                    <p>
                        Search for an active rental
                        and record the returned items.
                    </p>

                </div>

                <button
                    className="secondary-btn"
                    onClick={() =>
                        navigate("/returns")
                    }
                >
                    Back
                </button>

            </div>


            <div className="card">

                <h2>Find Rental</h2>

                <div className="search-row">

                    <input
                        type="text"
                        placeholder="Search by Rental ID, Company Name or Customer Name"
                        value={searchTerm}
                        onChange={(event) =>
                            setSearchTerm(
                                event.target.value
                            )
                        }
                        onKeyDown={(event) => {

                            if (
                                event.key ===
                                "Enter"
                            ) {

                                void handleSearch();
                            }
                        }}
                    />

                    <button
                        className="primary-btn"
                        disabled={searching}
                        onClick={() => {
                            void handleSearch();
                        }}
                    >
                        {
                            searching
                                ? "Searching..."
                                : "Search"
                        }
                    </button>

                </div>


                {
                    searchResults.length >
                    0 && (

                        <div className="search-results">

                            {
                                searchResults.map(
                                    (rental) => (

                                        <div
                                            className="search-result"
                                            key={
                                                rental.rentalId
                                            }
                                        >

                                            <div>

                                                <strong>
                                                    Rental #
                                                    {
                                                        rental.rentalId
                                                    }
                                                </strong>

                                                <p>
                                                    {
                                                        rental.companyName
                                                    }
                                                    {" — "}
                                                    {
                                                        rental.customerName
                                                    }
                                                </p>

                                                <p>
                                                    {
                                                        rental.startDate
                                                    }
                                                    {" to "}
                                                    {
                                                        rental.dueDate
                                                    }
                                                    {" — "}
                                                    {
                                                        rental.rentalStatus
                                                    }
                                                </p>

                                            </div>

                                            <button
                                                className="small-btn"
                                                disabled={
                                                    loadingRental
                                                }
                                                onClick={() => {
                                                    void handleSelectRental(
                                                        rental
                                                    );
                                                }}
                                            >
                                                Select
                                            </button>

                                        </div>
                                    )
                                )
                            }

                        </div>
                    )
                }

            </div>


            {
                error && (

                    <div className="error-message">
                        {error}
                    </div>
                )
            }


            {
                selectedRental && (

                    <>

                        <div className="card">

                            <h2>
                                Rental Details
                            </h2>

                            <div className="details-grid">

                                <div>
                                    <span>
                                        Rental ID
                                    </span>
                                    <strong>
                                        {
                                            selectedRental.rentalId
                                        }
                                    </strong>
                                </div>

                                <div>
                                    <span>
                                        Company
                                    </span>
                                    <strong>
                                        {
                                            selectedRental.companyName
                                        }
                                    </strong>
                                </div>

                                <div>
                                    <span>
                                        Customer
                                    </span>
                                    <strong>
                                        {
                                            selectedRental.customerName
                                        }
                                    </strong>
                                </div>

                                <div>
                                    <span>
                                        Phone
                                    </span>
                                    <strong>
                                        {
                                            selectedRental.customerPhone
                                        }
                                    </strong>
                                </div>

                                <div>
                                    <span>
                                        Start Date
                                    </span>
                                    <strong>
                                        {
                                            selectedRental.startDate
                                        }
                                    </strong>
                                </div>

                                <div>
                                    <span>
                                        Due Date
                                    </span>
                                    <strong>
                                        {
                                            selectedRental.dueDate
                                        }
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

                        </div>


                        <div className="card">

                            <div className="section-heading">

                                <h2>
                                    Returned Items
                                </h2>

                                <div
                                    className={
                                        calculateReturnType() ===
                                        "FULL"
                                            ? "type-badge full"
                                            : "type-badge partial"
                                    }
                                >
                                    {
                                        calculateReturnType()
                                    } RETURN
                                </div>

                            </div>


                            <div className="table-wrapper">

                                <table className="return-table">

                                    <thead>

                                    <tr>
                                        <th>Select</th>
                                        <th>Equipment</th>
                                        <th>Code</th>
                                        <th>Issued</th>
                                        <th>
                                            Previously Returned
                                        </th>
                                        <th>
                                            Outstanding
                                        </th>
                                        <th>
                                            Returning Now
                                        </th>
                                        <th>Condition</th>
                                        <th>
                                            Inspection Notes
                                        </th>
                                    </tr>

                                    </thead>

                                    <tbody>

                                    {
                                        returnItems.map(
                                            (item) => {

                                                const outstanding =
                                                    getOutstandingQuantity(
                                                        item
                                                    );

                                                return (

                                                    <tr
                                                        key={
                                                            item.rentalItemId
                                                        }
                                                    >

                                                        <td>

                                                            <input
                                                                type="checkbox"
                                                                checked={
                                                                    item.selected
                                                                }
                                                                disabled={
                                                                    outstanding ===
                                                                    0
                                                                }
                                                                onChange={() =>
                                                                    handleItemSelection(
                                                                        item.rentalItemId
                                                                    )
                                                                }
                                                            />

                                                        </td>

                                                        <td>
                                                            {
                                                                item.equipmentName
                                                            }
                                                        </td>

                                                        <td>
                                                            {
                                                                item.itemCode
                                                            }
                                                        </td>

                                                        <td>
                                                            {
                                                                item.issuedQuantity
                                                            }
                                                        </td>

                                                        <td>
                                                            {
                                                                item.alreadyReturnedQuantity
                                                            }
                                                        </td>

                                                        <td>
                                                            {
                                                                outstanding
                                                            }
                                                        </td>

                                                        <td>

                                                            <input
                                                                className="quantity-input"
                                                                type="number"
                                                                min="1"
                                                                max={
                                                                    outstanding
                                                                }
                                                                disabled={
                                                                    !item.selected
                                                                }
                                                                value={
                                                                    item.quantityToReturn
                                                                }
                                                                onChange={(event) =>
                                                                    handleQuantityChange(
                                                                        item.rentalItemId,
                                                                        event.target.value
                                                                    )
                                                                }
                                                            />

                                                        </td>

                                                        <td>

                                                            <select
                                                                disabled={
                                                                    !item.selected
                                                                }
                                                                value={
                                                                    item.conditionStatus
                                                                }
                                                                onChange={(event) =>
                                                                    handleConditionChange(
                                                                        item.rentalItemId,
                                                                        event.target.value
                                                                    )
                                                                }
                                                            >

                                                                <option value="GOOD">
                                                                    Good
                                                                </option>

                                                                <option value="DAMAGED">
                                                                    Damaged
                                                                </option>

                                                                <option value="MISSING PARTS">
                                                                    Missing Parts
                                                                </option>

                                                                <option value="NEEDS MAINTENANCE">
                                                                    Needs Maintenance
                                                                </option>

                                                            </select>

                                                        </td>

                                                        <td>

                                                            <input
                                                                type="text"
                                                                placeholder="Inspection notes"
                                                                disabled={
                                                                    !item.selected
                                                                }
                                                                value={
                                                                    item.inspectionNotes
                                                                }
                                                                onChange={(event) =>
                                                                    handleInspectionNotes(
                                                                        item.rentalItemId,
                                                                        event.target.value
                                                                    )
                                                                }
                                                            />

                                                        </td>

                                                    </tr>
                                                );
                                            }
                                        )
                                    }

                                    </tbody>

                                </table>

                            </div>

                        </div>


                        <div className="card">

                            <h2>
                                Return Notes
                            </h2>

                            <textarea
                                rows="4"
                                placeholder="Enter any additional notes about this return..."
                                value={generalNotes}
                                onChange={(event) =>
                                    setGeneralNotes(
                                        event.target.value
                                    )
                                }
                            />

                        </div>


                        <div className="action-row">

                            <button
                                className="secondary-btn"
                                disabled={saving}
                                onClick={() =>
                                    navigate("/returns")
                                }
                            >
                                Cancel
                            </button>

                            <button
                                className="primary-btn"
                                disabled={saving}
                                onClick={() => {
                                    void handleProcessReturn();
                                }}
                            >
                                {
                                    saving
                                        ? "Processing..."
                                        : "Process Return"
                                }
                            </button>

                        </div>

                    </>
                )
            }

        </div>
    );
}

export default ProcessReturn;