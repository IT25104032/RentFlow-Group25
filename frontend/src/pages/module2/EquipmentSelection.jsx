import React, { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import RentalStepper from "../../components/module2/RentalStepper";
import "./EquipmentSelection.css";

const EQUIPMENT_API =
    "/api/module1/equipment/available-for-rental";

function EquipmentSelection({ onBack }) {
    const location = useLocation();
    const navigate = useNavigate();

    // Equipment retrieved from Module 1
    const [equipmentData, setEquipmentData] = useState([]);
    const [loadingEquipment, setLoadingEquipment] = useState(true);
    const [equipmentError, setEquipmentError] = useState("");

    // Fetch the logged-in company's available equipment
    useEffect(() => {

        async function loadEquipment() {
            setLoadingEquipment(true);
            setEquipmentError("");

            try {
                const response = await fetch(EQUIPMENT_API, {
                    method: "GET",
                    credentials: "include"
                });

                if (!response.ok) {
                    throw new Error(
                        `Failed to load equipment (${response.status}).`
                    );
                }

                const data = await response.json();

                if (!Array.isArray(data)) {
                    throw new Error("Invalid equipment response.");
                }

                setEquipmentData(data);

            } catch (err) {
                console.error("Equipment loading failed:", err);
                setEquipmentError(err.message);
                setEquipmentData([]);

            } finally {
                setLoadingEquipment(false);
            }
        }

        loadEquipment();

    }, []);

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

    const [searchText, setSearchText] = useState("");
    const [selectedEquipment, setSelectedEquipment] = useState(
        draft.selectedEquipment || []);
    const [depositOverride, setDepositOverride] = useState(
        draft.depositOverride ?? null);
    const updateSelectedEquipment = (updatedEquipment) => {
        setSelectedEquipment(updatedEquipment);
        setDepositOverride(null);

        const recalculatedTotal = updatedEquipment.reduce(
            (total, item) =>
                total +
                Number(item.quantity || 0) *
                (Number(item.securityDepositPerUnit) || 0),
            0
        );

        sessionStorage.setItem(
            "rentFlowRentalDraft",
            JSON.stringify({
                ...draft,
                selectedRenter,
                rentalDetails,
                selectedEquipment: updatedEquipment,
                depositOverride: null,
                totalDeposit: recalculatedTotal
            })
        );
    };

    const [error, setError] =
        useState("");


     //Filter the temporary equipment list based on search text
    const filteredEquipment = useMemo(() => {

        const search =
            searchText.trim().toLowerCase();

        if (!search) {
            return equipmentData;
        }

        return equipmentData.filter(
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

    }, [searchText, equipmentData]);

     //Add an equipment item to the rental.
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

        updateSelectedEquipment([
            ...selectedEquipment,
            {
                ...equipment,
                quantity: 1,
                securityDepositPerUnit:
                    Number(equipment.securityDepositPerUnit)
            }
        ]);

    };


    //Change the quantity of a selected equipment item
    const handleQuantityChange = (equipmentId, quantity) => {
        setError("");

        const numericQuantity = Number(quantity);

        const updatedEquipment = selectedEquipment.map((item) => {
            if (item.equipmentId !== equipmentId) {
                return item;
            }

            if (numericQuantity < 1) {
                return { ...item, quantity: 1 };
            }

            if (numericQuantity > item.availableQuantity) {
                setError(
                    `${item.itemName} has only ${item.availableQuantity} unit(s) available.`
                );

                return {
                    ...item,
                    quantity: item.availableQuantity
                };
            }

            return { ...item, quantity: numericQuantity };
        });

        updateSelectedEquipment(updatedEquipment);
    };


    // Remove an equipment item from the rental selection
    const handleRemoveEquipment = (equipmentId) => {
        setError("");

        const updatedEquipment = selectedEquipment.filter(
            (item) => item.equipmentId !== equipmentId
        );

        updateSelectedEquipment(updatedEquipment);
    };


    //Calculate the total security deposit for all selected items
    const calculatedTotalDeposit = useMemo(() => {
        return selectedEquipment.reduce((total, item) => {
            return total +
                Number(item.quantity || 0) *
                (Number(item.securityDepositPerUnit) || 0);
        }, 0);
    }, [selectedEquipment]);

    const totalDeposit =
        depositOverride === null
            ? calculatedTotalDeposit
            : depositOverride;

    /*
     * Continue to the review step.
     *
     * We do NOT create the rental in the
     * backend here yet.
     */
    const handleContinue = () => {

        setError("");

        if (loadingEquipment || equipmentError) {
            setError("Equipment information is not available.");
            return;
        }

        if (selectedEquipment.length === 0) {
            setError("Please select at least one equipment item.");
            return;
        }

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


        const missingDeposit = selectedEquipment.some(
            (item) =>
                item.securityDepositPerUnit == null ||
                Number(item.securityDepositPerUnit) < 0
        );

        if (missingDeposit) {

            setError(
                "Please enter the security deposit per unit for all selected equipment."
            );

            return;
        }

        sessionStorage.setItem(
            "rentFlowRentalDraft",
            JSON.stringify({
                ...draft,
                selectedRenter,
                rentalDetails,
                selectedEquipment,
                depositOverride,
                totalDeposit
            })
        );

        navigate(
            "/rentals/review",
            {
                state: {
                    selectedRenter: selectedRenter,
                    rentalDetails: rentalDetails,
                    selectedEquipment: selectedEquipment,
                    totalDeposit: totalDeposit
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


            <RentalStepper activeStep={3}/>

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


            {loadingEquipment && (
                <p>Loading available equipment...</p>
            )}

            {equipmentError && (
                <div className="error-message">
                    {equipmentError}
                </div>
            )}

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
                            <th>Equipment</th>
                            <th>Code</th>
                            <th>Category</th>
                            <th>Available</th>
                            <th>Rate</th>
                            <th>Security Deposit/Unit</th>
                            <th>Action</th>
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
                                            {equipment.itemName}
                                        </td>

                                        <td>
                                            {equipment.itemCode}
                                        </td>

                                        <td>
                                            {equipment.categoryName}
                                        </td>

                                        <td>
                                            {equipment.availableQuantity}
                                        </td>

                                        <td>
                                            Rs.{" "}
                                            {equipment.rentalRate.toLocaleString()}
                                            /
                                            {equipment.ratePeriod.toLowerCase()}
                                        </td>

                                        <td>
                                            Rs. {Number(
                                            equipment.securityDepositPerUnit
                                        ).toLocaleString()}
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
                                <th>Equipment</th>
                                <th>Available</th>
                                <th>Quantity</th>
                                <th>Rate</th>
                                <th>Security Deposit/Unit</th>
                                <th>Action</th>
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
                                                {item.itemName}
                                            </strong>

                                            <br />

                                            <span>
                                                    {item.itemCode}
                                                </span>

                                        </td>

                                        <td>
                                            {item.availableQuantity}
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
                                            {item.rentalRate.toLocaleString()}
                                            /
                                            {item.ratePeriod.toLowerCase()}
                                        </td>

                                        <td>
                                            Rs. {Number(
                                            item.securityDepositPerUnit
                                        ).toLocaleString()}
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
                        <div>
                            <label htmlFor="totalSecurityDeposit">
                                Total Security Deposit (Rs.)
                            </label>

                            <input
                                id="totalSecurityDeposit"
                                type="number"
                                min="0"
                                step="0.01"
                                value={totalDeposit}
                                onChange={(event) => {
                                    const value = event.target.value;
                                    const newOverride = value === ""
                                        ? 0
                                        : Math.max(0, Number(value));

                                    setDepositOverride(newOverride);

                                    const updatedDraft = {
                                        ...draft,
                                        selectedRenter,
                                        rentalDetails,
                                        selectedEquipment,
                                        depositOverride: newOverride,
                                        totalDeposit: newOverride
                                    };

                                    sessionStorage.setItem(
                                        "rentFlowRentalDraft",
                                        JSON.stringify(updatedDraft)
                                    );
                                }}
                            />
                        </div>

                        <small>
                            Automatically calculated from the selected quantities and
                            hardcoded deposits per unit. You can adjust the total before
                            reviewing the rental.
                        </small>
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
                                        selectedRenter: selectedRenter,
                                        rentalDetails: rentalDetails,
                                        selectedEquipment: selectedEquipment
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