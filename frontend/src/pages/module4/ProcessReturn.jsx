import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { mockRentals } from "../../data/mockRentals";
import {
    createReturn,
    createReturnItem
} from "../../services/module4/module4Api.js";

import "../../styles/module4/module4.css";


function ProcessReturn() {

    const navigate = useNavigate();

    const [searchTerm, setSearchTerm] = useState("");
    const [searchResults, setSearchResults] = useState([]);
    const [selectedRental, setSelectedRental] = useState(null);

    const [returnItems, setReturnItems] = useState([]);

    const [generalNotes, setGeneralNotes] = useState("");

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [saving, setSaving] = useState(false);


    // TEMPORARY until real login is implemented
    const CURRENT_USER_ID = 3;


    // -------------------------------------------------
    // SEARCH RENTALS
    // -------------------------------------------------

    function handleSearch() {

        setError("");

        if (!searchTerm.trim()) {
            setSearchResults([]);
            return;
        }

        const value = searchTerm.toLowerCase();

        const results = mockRentals.filter((rental) =>

            rental.rentalId
                .toString()
                .includes(value)

            ||

            rental.companyName
                .toLowerCase()
                .includes(value)

            ||

            rental.customerName
                .toLowerCase()
                .includes(value)

        );

        setSearchResults(results);
    }


    // -------------------------------------------------
    // SELECT RENTAL
    // -------------------------------------------------

    function handleSelectRental(rental) {

        setSelectedRental(rental);

        const preparedItems = rental.items.map((item) => ({
            ...item,

            selected: false,

            quantityToReturn: 0,

            conditionStatus: "GOOD",

            inspectionNotes: ""
        }));

        setReturnItems(preparedItems);

        setSearchResults([]);
        setError("");
        setSuccess("");
    }


    // -------------------------------------------------
    // CALCULATE OUTSTANDING QUANTITY
    // -------------------------------------------------

    function getOutstandingQuantity(item) {

        return (
            item.quantityRented -
            item.quantityAlreadyReturned
        );
    }


    // -------------------------------------------------
    // UPDATE CHECKBOX
    // -------------------------------------------------

    function handleItemSelection(rentalItemId) {

        setReturnItems((previousItems) =>

            previousItems.map((item) => {

                if (item.rentalItemId === rentalItemId) {

                    return {
                        ...item,
                        selected: !item.selected,

                        quantityToReturn:
                            !item.selected
                                ? getOutstandingQuantity(item)
                                : 0
                    };
                }

                return item;
            })
        );
    }


    // -------------------------------------------------
    // UPDATE QUANTITY
    // -------------------------------------------------

    function handleQuantityChange(
        rentalItemId,
        quantity
    ) {

        setReturnItems((previousItems) =>

            previousItems.map((item) => {

                if (item.rentalItemId === rentalItemId) {

                    return {
                        ...item,
                        quantityToReturn: Number(quantity)
                    };
                }

                return item;
            })
        );
    }


    // -------------------------------------------------
    // UPDATE CONDITION
    // -------------------------------------------------

    function handleConditionChange(
        rentalItemId,
        condition
    ) {

        setReturnItems((previousItems) =>

            previousItems.map((item) => {

                if (item.rentalItemId === rentalItemId) {

                    return {
                        ...item,
                        conditionStatus: condition
                    };
                }

                return item;
            })
        );
    }


    // -------------------------------------------------
    // UPDATE INSPECTION NOTES
    // -------------------------------------------------

    function handleInspectionNotes(
        rentalItemId,
        notes
    ) {

        setReturnItems((previousItems) =>

            previousItems.map((item) => {

                if (item.rentalItemId === rentalItemId) {

                    return {
                        ...item,
                        inspectionNotes: notes
                    };
                }

                return item;
            })
        );
    }


    // -------------------------------------------------
    // CALCULATE RETURN TYPE
    // -------------------------------------------------

    function calculateReturnType() {

        if (!selectedRental) {
            return "";
        }

        let totalOutstandingBeforeReturn = 0;
        let totalReturningNow = 0;

        returnItems.forEach((item) => {

            const outstanding =
                getOutstandingQuantity(item);

            totalOutstandingBeforeReturn += outstanding;

            if (item.selected) {

                totalReturningNow +=
                    Number(item.quantityToReturn);
            }
        });


        if (
            totalReturningNow > 0 &&
            totalReturningNow ===
            totalOutstandingBeforeReturn
        ) {
            return "FULL";
        }

        return "PARTIAL";
    }


    // -------------------------------------------------
    // VALIDATE
    // -------------------------------------------------

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
                getOutstandingQuantity(item);


            if (item.quantityToReturn <= 0) {

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


    // -------------------------------------------------
    // PROCESS RETURN
    // -------------------------------------------------

    async function handleProcessReturn() {

        setError("");
        setSuccess("");


        if (!validateReturn()) {
            return;
        }


        try {

            setSaving(true);


            const returnType =
                calculateReturnType();


            // -----------------------------------------
            // 1. CREATE RETURN HEADER
            // -----------------------------------------

            const returnData = {

                rentalId:
                selectedRental.rentalId,

                processedBy:
                CURRENT_USER_ID,

                returnType:
                returnType,

                notes:
                generalNotes
            };


            const savedReturn =
                await createReturn(returnData);


            // -----------------------------------------
            // 2. CREATE RETURN ITEM RECORDS
            // -----------------------------------------

            const selectedItems =
                returnItems.filter(
                    (item) => item.selected
                );


            for (const item of selectedItems) {

                const returnItemData = {

                    returnId:
                    savedReturn.returnId,

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
                };


                await createReturnItem(
                    returnItemData
                );
            }


            setSuccess(
                `Return #${savedReturn.returnId} successfully recorded as ${returnType}.`
            );


            setTimeout(() => {

                navigate("/returns");

            }, 1500);


        } catch (err) {

            console.error(err);

            setError(
                "Unable to process return. Please check the backend."
            );

        } finally {

            setSaving(false);
        }
    }


    // -------------------------------------------------
    // UI
    // -------------------------------------------------

    return (

        <div className="module-page">

            <div className="page-header">

                <div>

                    <h1>Process Rental Return</h1>

                    <p>
                        Search for an active rental and record
                        the items being returned.
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


            {/* SEARCH SECTION */}

            <div className="card">

                <h2>Find Rental</h2>

                <div className="search-row">

                    <input
                        type="text"
                        placeholder="Search by Rental ID, Company Name or Customer Name"
                        value={searchTerm}
                        onChange={(e) =>
                            setSearchTerm(
                                e.target.value
                            )
                        }
                    />

                    <button
                        className="primary-btn"
                        onClick={handleSearch}
                    >
                        Search
                    </button>

                </div>


                {searchResults.length > 0 && (

                    <div className="search-results">

                        {searchResults.map(
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

                                    </div>


                                    <button
                                        className="small-btn"
                                        onClick={() =>
                                            handleSelectRental(
                                                rental
                                            )
                                        }
                                    >
                                        Select
                                    </button>

                                </div>
                            )
                        )}

                    </div>

                )}

            </div>


            {/* RENTAL DETAILS */}

            {selectedRental && (

                <>

                    <div className="card">

                        <h2>Rental Details</h2>

                        <div className="details-grid">

                            <div>
                                <span>Rental ID</span>
                                <strong>
                                    {
                                        selectedRental.rentalId
                                    }
                                </strong>
                            </div>


                            <div>
                                <span>Company</span>
                                <strong>
                                    {
                                        selectedRental.companyName
                                    }
                                </strong>
                            </div>


                            <div>
                                <span>Customer</span>
                                <strong>
                                    {
                                        selectedRental.customerName
                                    }
                                </strong>
                            </div>


                            <div>
                                <span>Phone</span>
                                <strong>
                                    {
                                        selectedRental.customerPhone
                                    }
                                </strong>
                            </div>


                            <div>
                                <span>Start Date</span>
                                <strong>
                                    {
                                        selectedRental.startDate
                                    }
                                </strong>
                            </div>


                            <div>
                                <span>Due Date</span>
                                <strong>
                                    {
                                        selectedRental.dueDate
                                    }
                                </strong>
                            </div>


                            <div>
                                <span>Status</span>
                                <strong>
                                    {
                                        selectedRental.rentalStatus
                                    }
                                </strong>
                            </div>

                        </div>

                    </div>


                    {/* RETURN ITEMS */}

                    <div className="card">

                        <div className="section-heading">

                            <h2>Returned Items</h2>

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

                            <table
                                className="return-table"
                            >

                                <thead>

                                <tr>

                                    <th>Select</th>
                                    <th>Equipment</th>
                                    <th>Code</th>
                                    <th>Rented</th>
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

                                {returnItems.map(
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
                                                        item.quantityRented
                                                    }
                                                </td>


                                                <td>
                                                    {
                                                        item.quantityAlreadyReturned
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
                                                        onChange={(
                                                            e
                                                        ) =>
                                                            handleQuantityChange(
                                                                item.rentalItemId,
                                                                e
                                                                    .target
                                                                    .value
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
                                                        onChange={(
                                                            e
                                                        ) =>
                                                            handleConditionChange(
                                                                item.rentalItemId,
                                                                e
                                                                    .target
                                                                    .value
                                                            )
                                                        }
                                                    >

                                                        <option value="GOOD">
                                                            Good
                                                        </option>

                                                        <option value="DAMAGED">
                                                            Damaged
                                                        </option>

                                                        <option value="MISSING_PARTS">
                                                            Missing Parts
                                                        </option>

                                                        <option value="NEEDS_MAINTENANCE">
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
                                                        onChange={(
                                                            e
                                                        ) =>
                                                            handleInspectionNotes(
                                                                item.rentalItemId,
                                                                e
                                                                    .target
                                                                    .value
                                                            )
                                                        }
                                                    />

                                                </td>

                                            </tr>

                                        );
                                    }
                                )}

                                </tbody>

                            </table>

                        </div>

                    </div>


                    {/* GENERAL NOTES */}

                    <div className="card">

                        <h2>Return Notes</h2>

                        <textarea
                            rows="4"
                            placeholder="Enter any additional notes about this return..."
                            value={generalNotes}
                            onChange={(e) =>
                                setGeneralNotes(
                                    e.target.value
                                )
                            }
                        />

                    </div>


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


                    <div className="action-row">

                        <button
                            className="secondary-btn"
                            onClick={() =>
                                navigate("/returns")
                            }
                        >
                            Cancel
                        </button>


                        <button
                            className="primary-btn"
                            disabled={saving}
                            onClick={
                                handleProcessReturn
                            }
                        >

                            {saving
                                ? "Processing..."
                                : "Process Return"}

                        </button>

                    </div>

                </>

            )}

        </div>
    );
}

export default ProcessReturn;