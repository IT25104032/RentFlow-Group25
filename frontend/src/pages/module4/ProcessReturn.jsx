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


    // -------------------------------------------------
    // STATE
    // -------------------------------------------------

    const [searchTerm, setSearchTerm] = useState("");

    const [searchResults, setSearchResults] = useState([]);

    const [selectedRental, setSelectedRental] = useState(null);

    const [returnItems, setReturnItems] = useState([]);

    const [generalNotes, setGeneralNotes] = useState("");

    const [error, setError] = useState("");

    const [saving, setSaving] = useState(false);

    const [searching, setSearching] = useState(false);

    const [loadingRental, setLoadingRental] = useState(false);


    // TEMPORARY
    // Replace this later with the logged-in user's ID
    const CURRENT_USER_ID = 3;



    // -------------------------------------------------
    // SEARCH RENTALS
    // -------------------------------------------------

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
                "Unable to search rentals. Please check the backend."
            );

        } finally {

            setSearching(false);
        }
    }



    // -------------------------------------------------
    // SELECT RENTAL
    // -------------------------------------------------

    async function handleSelectRental(rental) {

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
                "Unable to load rental details."
            );

        } finally {

            setLoadingRental(false);
        }
    }



    // -------------------------------------------------
    // GET OUTSTANDING QUANTITY
    // -------------------------------------------------

    function getOutstandingQuantity(item) {

        return item.remainingQuantity;
    }



    // -------------------------------------------------
    // SELECT / UNSELECT RETURN ITEM
    // -------------------------------------------------

    function handleItemSelection(rentalItemId) {

        setReturnItems((previousItems) =>

            previousItems.map((item) => {

                if (
                    item.rentalItemId ===
                    rentalItemId
                ) {

                    const willBeSelected =
                        !item.selected;


                    return {

                        ...item,

                        selected:
                        willBeSelected,

                        quantityToReturn:
                            willBeSelected
                                ? getOutstandingQuantity(item)
                                : 0,

                        conditionStatus:
                            willBeSelected
                                ? item.conditionStatus
                                : "GOOD",

                        inspectionNotes:
                            willBeSelected
                                ? item.inspectionNotes
                                : ""
                    };
                }

                return item;
            })
        );
    }



    // -------------------------------------------------
    // UPDATE RETURN QUANTITY
    // -------------------------------------------------

    function handleQuantityChange(
        rentalItemId,
        quantity
    ) {

        setReturnItems((previousItems) =>

            previousItems.map((item) => {

                if (
                    item.rentalItemId ===
                    rentalItemId
                ) {

                    return {

                        ...item,

                        quantityToReturn:
                            Number(quantity)
                    };
                }

                return item;
            })
        );
    }



    // -------------------------------------------------
    // UPDATE ITEM CONDITION
    // -------------------------------------------------

    function handleConditionChange(
        rentalItemId,
        condition
    ) {

        setReturnItems((previousItems) =>

            previousItems.map((item) => {

                if (
                    item.rentalItemId ===
                    rentalItemId
                ) {

                    return {

                        ...item,

                        conditionStatus:
                        condition
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

                if (
                    item.rentalItemId ===
                    rentalItemId
                ) {

                    return {

                        ...item,

                        inspectionNotes:
                        notes
                    };
                }

                return item;
            })
        );
    }



    // -------------------------------------------------
    // CALCULATE RETURN TYPE FOR UI DISPLAY
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


            totalOutstandingBeforeReturn +=
                outstanding;


            if (item.selected) {

                totalReturningNow +=
                    Number(
                        item.quantityToReturn
                    );
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
    // VALIDATE RETURN
    // -------------------------------------------------

    function validateReturn() {

        const selectedItems =
            returnItems.filter(
                (item) => item.selected
            );


        if (
            selectedItems.length === 0
        ) {

            setError(
                "Please select at least one item."
            );

            return false;
        }


        for (
            const item of selectedItems
            ) {

            const outstanding =
                getOutstandingQuantity(item);


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


            if (
                !item.conditionStatus
            ) {

                setError(
                    `Please select a condition for ${item.equipmentName}.`
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


            // -------------------------------------------------
            // IF THERE ARE DAMAGED ITEMS
            // -------------------------------------------------

            if (
                result.damagedReturnItemIds &&
                result.damagedReturnItemIds.length > 0
            ) {

                const savedItems =
                    await getReturnItems(
                        result.returnId
                    );


                const damagedItems =
                    savedItems.filter(
                        (item) =>
                            result
                                .damagedReturnItemIds
                                .includes(
                                    item.returnItemId
                                )
                    );


                navigate(
                    `/returns/${result.returnId}/damages`,
                    {
                        state: {
                            damagedItems
                        }
                    }
                );

            } else {

                // No damaged items
                // Go directly to return details

                navigate(
                    `/returns/${result.returnId}`
                );
            }


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


            {/* PAGE HEADER */}

            <div className="page-header">

                <div>

                    <h1>
                        Process Rental Return
                    </h1>

                    <p>
                        Search for a rental and record
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

                <h2>
                    Find Rental
                </h2>


                <div className="search-row">

                    <input
                        type="text"
                        placeholder="Search by Rental ID or Customer Name"
                        value={searchTerm}
                        onChange={(e) =>
                            setSearchTerm(
                                e.target.value
                            )
                        }
                        onKeyDown={(e) => {

                            if (
                                e.key === "Enter"
                            ) {

                                handleSearch();
                            }
                        }}
                    />


                    <button
                        className="primary-btn"
                        onClick={handleSearch}
                        disabled={searching}
                    >

                        {
                            searching
                                ? "Searching..."
                                : "Search"
                        }

                    </button>

                </div>



                {/* SEARCH RESULTS */}

                {
                    searchResults.length > 0 && (

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
                                                        rental.customerName
                                                    }

                                                    {" — "}

                                                    {
                                                        rental.rentalStatus
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

                                                </p>

                                            </div>


                                            <button
                                                className="small-btn"
                                                disabled={
                                                    loadingRental
                                                }
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
                                )
                            }

                        </div>
                    )
                }

            </div>



            {/* ERROR MESSAGE */}

            {
                error && (

                    <div className="error-message">

                        {error}

                    </div>
                )
            }



            {/* RENTAL DETAILS */}

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



                        {/* RETURNED ITEMS */}

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

                                        <th>
                                            Select
                                        </th>

                                        <th>
                                            Equipment
                                        </th>

                                        <th>
                                            Issued
                                        </th>

                                        <th>
                                            Previously Returned
                                        </th>

                                        <th>
                                            Outstanding
                                        </th>

                                        <th>
                                            Returning Now
                                        </th>

                                        <th>
                                            Condition
                                        </th>

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


                                                        {/* SELECT */}

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



                                                        {/* EQUIPMENT */}

                                                        <td>

                                                            {
                                                                item.equipmentName
                                                            }

                                                        </td>



                                                        {/* ISSUED QUANTITY */}

                                                        <td>

                                                            {
                                                                item.issuedQuantity
                                                            }

                                                        </td>



                                                        {/* ALREADY RETURNED */}

                                                        <td>

                                                            {
                                                                item.alreadyReturnedQuantity
                                                            }

                                                        </td>



                                                        {/* OUTSTANDING */}

                                                        <td>

                                                            {
                                                                outstanding
                                                            }

                                                        </td>



                                                        {/* RETURNING NOW */}

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
                                                                onChange={(e) =>
                                                                    handleQuantityChange(
                                                                        item.rentalItemId,
                                                                        e.target.value
                                                                    )
                                                                }
                                                            />

                                                        </td>



                                                        {/* CONDITION */}

                                                        <td>

                                                            <select
                                                                disabled={
                                                                    !item.selected
                                                                }
                                                                value={
                                                                    item.conditionStatus
                                                                }
                                                                onChange={(e) =>
                                                                    handleConditionChange(
                                                                        item.rentalItemId,
                                                                        e.target.value
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



                                                        {/* INSPECTION NOTES */}

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
                                                                onChange={(e) =>
                                                                    handleInspectionNotes(
                                                                        item.rentalItemId,
                                                                        e.target.value
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



                        {/* GENERAL RETURN NOTES */}

                        <div className="card">

                            <h2>
                                Return Notes
                            </h2>


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



                        {/* ACTION BUTTONS */}

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
                                onClick={
                                    handleProcessReturn
                                }
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