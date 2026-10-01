import React, { useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import RentalStepper from "../../components/module2/RentalStepper";
import "./EquipmentSelection.css";

/*
 * Temporary equipment data.
 *
 * Later, this will come from the real
 * Equipment API provided by Module 1.
 */
const EQUIPMENT_DATA = [
    {
        equipmentId: 1,
        itemName: "Electric Drill",
        itemCode: "DRILL001",
        categoryName: "Power Tools",
        rentalRate: 1500,
        ratePeriod: "DAY",
        securityDepositPerUnit: 5000,
        availableQuantity: 10
    },
    {
        equipmentId: 2,
        itemName: "Angle Grinder",
        itemCode: "GRIND001",
        categoryName: "Power Tools",
        rentalRate: 1200,
        ratePeriod: "DAY",
        securityDepositPerUnit: 4000,
        availableQuantity: 7
    },
    {
        equipmentId: 3,
        itemName: "Concrete Mixer",
        itemCode: "MIX001",
        categoryName: "Construction Equipment",
        rentalRate: 5000,
        ratePeriod: "DAY",
        securityDepositPerUnit: 15000,
        availableQuantity: 4
    },
    {
        equipmentId: 4,
        itemName: "Pressure Washer",
        itemCode: "WASH001",
        categoryName: "Cleaning Equipment",
        rentalRate: 2500,
        ratePeriod: "DAY",
        securityDepositPerUnit: 8000,
        availableQuantity: 5
    },
    {
        equipmentId: 10,
        itemName: "Digital Camera",
        itemCode: "CAMERA001",
        categoryName: "Photography Equipment",
        rentalRate: 2000,
        ratePeriod: "DAY",
        securityDepositPerUnit: 6000,
        availableQuantity: 4
    }
];


function EquipmentSelection({ onBack }) {

    const location = useLocation();
    const navigate = useNavigate();

    /*
     * Receive the renter and rental details
     * that were collected in Step 1 and Step 2.
     */
    const selectedRenter =
        location.state?.selectedRenter || null;

    const rentalDetails =
        location.state?.rentalDetails || null;


    /*
     * Search text entered by the staff user.
     */
    const [searchText, setSearchText] =
        useState("");


    /*
     * Equipment currently selected for
     * this rental.
     */
    const [selectedEquipment, setSelectedEquipment] =
        useState(
            location.state?.selectedEquipment || []
        );


    const [error, setError] =
        useState("");


    /*
     * Filter the temporary equipment list
     * based on the search text.
     */
    const filteredEquipment = useMemo(() => {

        const search =
            searchText.trim().toLowerCase();

        if (!search) {
            return EQUIPMENT_DATA;
        }

        return EQUIPMENT_DATA.filter(
            (equipment) =>
                equipment.itemName
                    .toLowerCase()
                    .includes(search)
                ||
                equipment.itemCode
                    .toLowerCase()
                    .includes(search)
                ||
                equipment.categoryName
                    .toLowerCase()
                    .includes(search)
        );

    }, [searchText]);


    /*
     * Add an equipment item to the rental.
     */
    const handleAddEquipment = (equipment) => {

        setError("");

        const alreadySelected =
            selectedEquipment.some(
                (item) =>
                    item.equipmentId ===
                    equipment.equipmentId
            );

        if (alreadySelected) {

            setError(
                "This equipment has already been selected."
            );

            return;
        }


        setSelectedEquipment([
            ...selectedEquipment,
            {
                ...equipment,
                quantity: 1
            }
        ]);
    };


    /*
     * Change the quantity of a selected
     * equipment item.
     */
    const handleQuantityChange = (
        equipmentId,
        quantity
    ) => {

        setError("");

        const numericQuantity =
            Number(quantity);


        setSelectedEquipment(
            selectedEquipment.map(
                (item) => {

                    if (
                        item.equipmentId !==
                        equipmentId
                    ) {
                        return item;
                    }


                    if (
                        numericQuantity < 1
                    ) {
                        return {
                            ...item,
                            quantity: 1
                        };
                    }


                    if (
                        numericQuantity >
                        item.availableQuantity
                    ) {

                        setError(
                            `${item.itemName} has only ${item.availableQuantity} unit(s) available.`
                        );

                        return {
                            ...item,
                            quantity:
                            item.availableQuantity
                        };
                    }


                    return {
                        ...item,
                        quantity:
                        numericQuantity
                    };
                }
            )
        );
    };


    /*
     * Remove equipment from the rental.
     */
    const handleRemoveEquipment = (
        equipmentId
    ) => {

        setError("");

        setSelectedEquipment(
            selectedEquipment.filter(
                (item) =>
                    item.equipmentId !==
                    equipmentId
            )
        );
    };


    /*
     * Calculate the total refundable
     * deposit for all selected items.
     */
    const totalDeposit = useMemo(() => {

        return selectedEquipment.reduce(
            (total, item) =>
                total +
                (
                    item.quantity *
                    item.securityDepositPerUnit
                ),
            0
        );

    }, [selectedEquipment]);


    /*
     * Continue to the review step.
     *
     * We do NOT create the rental in the
     * backend here yet.
     */
    const handleContinue = () => {

        setError("");

        if (!selectedRenter) {

            setError(
                "No renter was selected."
            );

            return;
        }


        if (!rentalDetails) {

            setError(
                "Rental details were not found."
            );

            return;
        }


        if (
            selectedEquipment.length === 0
        ) {

            setError(
                "Please select at least one equipment item."
            );

            return;
        }


        navigate(
            "/rentals/review",
            {
                state: {
                    selectedRenter:
                    selectedRenter,

                    rentalDetails:
                    rentalDetails,

                    selectedEquipment:
                    selectedEquipment,

                    totalDeposit:
                    totalDeposit
                }
            }
        );
    };


    return (
        <div className="create-rental-page">

            <div className="page-header">

                <div>

                    <h1>
                        Create Rental
                    </h1>

                    <p>
                        Select the equipment
                        required for this rental.
                    </p>

                </div>

            </div>


            <RentalStepper
                activeStep={3}
            />


            {/* Selected renter card */}

            <div className="rental-card">

                <div className="card-header">

                    <div>

                        <h2>
                            Selected Renter
                        </h2>

                        <p>
                            Equipment will be
                            added to this rental.
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
                                    Rental Period
                                </span>

                                <span className="field-value">

                                    {
                                        rentalDetails?.startDate
                                    }

                                    {" → "}

                                    {
                                        rentalDetails?.dueDate
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

            </div>


            {/* Equipment search */}

            <div className="rental-card">

                <div className="card-header">

                    <div>

                        <h2>
                            Available Equipment
                        </h2>

                        <p>
                            Search and select
                            equipment for this rental.
                        </p>

                    </div>

                </div>


                <div className="form-group">

                    <label htmlFor="equipmentSearch">
                        Search Equipment
                    </label>

                    <input
                        id="equipmentSearch"
                        type="text"
                        value={searchText}
                        onChange={(event) =>
                            setSearchText(
                                event.target.value
                            )
                        }
                        placeholder="Search by name, code or category"
                    />

                </div>


                <div className="equipment-table-wrapper">

                    <table className="equipment-table">

                        <thead>

                        <tr>

                            <th>
                                Equipment
                            </th>

                            <th>
                                Code
                            </th>

                            <th>
                                Category
                            </th>

                            <th>
                                Available
                            </th>

                            <th>
                                Rate
                            </th>

                            <th>
                                Deposit / Unit
                            </th>

                            <th>
                                Action
                            </th>

                        </tr>

                        </thead>


                        <tbody>

                        {filteredEquipment.length > 0 ? (

                            filteredEquipment.map(
                                (equipment) => (

                                    <tr
                                        key={
                                            equipment.equipmentId
                                        }
                                    >

                                        <td>
                                            {
                                                equipment.itemName
                                            }
                                        </td>

                                        <td>
                                            {
                                                equipment.itemCode
                                            }
                                        </td>

                                        <td>
                                            {
                                                equipment.categoryName
                                            }
                                        </td>

                                        <td>
                                            {
                                                equipment.availableQuantity
                                            }
                                        </td>

                                        <td>
                                            Rs.{" "}
                                            {
                                                equipment.rentalRate.toLocaleString()
                                            }
                                            /
                                            {
                                                equipment.ratePeriod.toLowerCase()
                                            }
                                        </td>

                                        <td>
                                            Rs.{" "}
                                            {
                                                equipment.securityDepositPerUnit.toLocaleString()
                                            }
                                        </td>

                                        <td>

                                            <button
                                                type="button"
                                                onClick={() =>
                                                    handleAddEquipment(
                                                        equipment
                                                    )
                                                }
                                            >
                                                Add
                                            </button>

                                        </td>

                                    </tr>

                                )
                            )

                        ) : (

                            <tr>

                                <td
                                    colSpan="7"
                                    className="empty-table-message"
                                >
                                    No equipment found.
                                </td>

                            </tr>

                        )}

                        </tbody>

                    </table>

                </div>

            </div>


            {/* Selected equipment */}

            <div className="rental-card">

                <div className="card-header">

                    <div>

                        <h2>
                            Selected Equipment
                        </h2>

                        <p>
                            Set the quantity for
                            each selected item.
                        </p>

                    </div>

                </div>


                {selectedEquipment.length > 0 ? (

                    <div className="equipment-table-wrapper">

                        <table className="equipment-table">

                            <thead>

                            <tr>

                                <th>
                                    Equipment
                                </th>

                                <th>
                                    Available
                                </th>

                                <th>
                                    Quantity
                                </th>

                                <th>
                                    Rate
                                </th>

                                <th>
                                    Security Deposit
                                </th>

                                <th>
                                    Action
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

                                            <br />

                                            <span>
                                                    {
                                                        item.itemCode
                                                    }
                                                </span>

                                        </td>

                                        <td>
                                            {
                                                item.availableQuantity
                                            }
                                        </td>

                                        <td>

                                            <input
                                                type="number"
                                                min="1"
                                                max={
                                                    item.availableQuantity
                                                }
                                                value={
                                                    item.quantity
                                                }
                                                onChange={(
                                                    event
                                                ) =>
                                                    handleQuantityChange(
                                                        item.equipmentId,
                                                        event.target.value
                                                    )
                                                }
                                                style={{
                                                    width:
                                                        "80px"
                                                }}
                                            />

                                        </td>

                                        <td>
                                            Rs.{" "}
                                            {
                                                item.rentalRate.toLocaleString()
                                            }
                                            /
                                            {
                                                item.ratePeriod.toLowerCase()
                                            }
                                        </td>

                                        <td>
                                            Rs.{" "}
                                            {
                                                (
                                                    item.quantity *
                                                    item.securityDepositPerUnit
                                                ).toLocaleString()
                                            }
                                        </td>

                                        <td>

                                            <button
                                                type="button"
                                                onClick={() =>
                                                    handleRemoveEquipment(
                                                        item.equipmentId
                                                    )
                                                }
                                            >
                                                Remove
                                            </button>

                                        </td>

                                    </tr>

                                )
                            )}

                            </tbody>

                        </table>

                    </div>

                ) : (

                    <div className="empty-message">
                        No equipment selected yet.
                    </div>

                )}


                {selectedEquipment.length > 0 && (

                    <div className="deposit-summary">

                        <span>
                            Total Refundable Deposit
                        </span>

                        <strong>
                            Rs.{" "}
                            {
                                totalDeposit.toLocaleString()
                            }
                        </strong>

                    </div>

                )}

            </div>


            {error && (

                <div className="error-message">
                    {error}
                </div>

            )}


            <div className="form-actions">

                <button
                    type="button"
                    onClick={() =>
                        onBack
                            ? onBack()
                            : navigate(
                                "/rentals/create",
                                {
                                    state: {
                                        selectedRenter:
                                        selectedRenter,

                                        rentalDetails:
                                        rentalDetails,

                                        selectedEquipment:
                                        selectedEquipment
                                    }
                                }
                            )
                    }
                >
                    Back
                </button>



                <button
                    type="button"
                    onClick={handleContinue}
                >
                    Continue to Review
                </button>

            </div>

        </div>
    );
}


export default EquipmentSelection;