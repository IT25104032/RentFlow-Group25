import {
    useLocation,
    useNavigate,
    useParams
} from "react-router-dom";

import { useState } from "react";

import {
    createDamageRecord
} from "../../services/module4/module4Api.js";

import "../../styles/module4/module4.css";


function DamageAssessment() {

    const { returnId } =
        useParams();

    const location =
        useLocation();

    const navigate =
        useNavigate();

    const damagedItems =
        location.state?.damagedItems ||
        [];

    const CURRENT_USER_ID = 3;

    const [damageForms, setDamageForms] =
        useState(
            damagedItems.map(
                (item) => ({
                    returnItemId:
                    item.returnItemId,
                    equipmentRentalItemId:
                    item.rentalItemId,
                    damagedQuantity:
                    item.quantityReturned,
                    maxQuantity:
                    item.quantityReturned,
                    damageDescription: "",
                    damageLevel: "MINOR",
                    estimatedCost: 0,
                    finalCharge: 0,
                    status: "ASSESSED"
                })
            )
        );

    const [saving, setSaving] =
        useState(false);

    const [error, setError] =
        useState("");


    function handleDamageChange(
        index,
        field,
        value
    ) {

        setDamageForms(
            (previous) => {

                const updated =
                    [...previous];

                updated[index] = {
                    ...updated[index],
                    [field]: value
                };

                return updated;
            }
        );
    }


    function validateDamageForms() {

        if (damageForms.length === 0) {

            setError(
                "No damaged return items were supplied."
            );

            return false;
        }

        for (const damage of damageForms) {

            if (
                Number(
                    damage.damagedQuantity
                ) <= 0
            ) {

                setError(
                    "Damaged quantity must be greater than zero."
                );

                return false;
            }

            if (
                Number(
                    damage.damagedQuantity
                ) >
                Number(
                    damage.maxQuantity
                )
            ) {

                setError(
                    "Damaged quantity cannot exceed the returned quantity."
                );

                return false;
            }

            if (
                !damage.damageDescription
                    .trim()
            ) {

                setError(
                    "Please enter a damage description."
                );

                return false;
            }
        }

        return true;
    }


    async function handleSaveDamages() {

        setError("");

        if (!validateDamageForms()) {
            return;
        }

        try {

            setSaving(true);

            for (
                const damage of damageForms
                ) {

                const damageData = {

                    returnItem: {
                        returnItemId:
                        damage.returnItemId
                    },

                    damagedQuantity:
                        Number(
                            damage.damagedQuantity
                        ),

                    damageDescription:
                    damage.damageDescription,

                    damageLevel:
                    damage.damageLevel,

                    estimatedCost:
                        Number(
                            damage.estimatedCost
                        ),

                    finalCharge:
                        Number(
                            damage.finalCharge
                        ),

                    assessedBy:
                    CURRENT_USER_ID,

                    assessmentDate:
                        new Date()
                            .toISOString(),

                    status:
                    damage.status
                };

                await createDamageRecord(
                    damageData
                );
            }

            navigate(
                `/returns/${returnId}`
            );

        } catch (err) {

            console.error(err);

            setError(
                err.message ||
                "Unable to save damage records."
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
                        Damage Assessment
                    </h1>

                    <p>
                        Return #{returnId}
                    </p>

                </div>

                <button
                    className="secondary-btn"
                    onClick={() =>
                        navigate(
                            `/returns/${returnId}`
                        )
                    }
                >
                    Back
                </button>

            </div>


            {
                error && (

                    <div className="error-message">
                        {error}
                    </div>
                )
            }


            {
                damageForms.length === 0 && (

                    <div className="card">

                        <p>
                            No damaged items are
                            available for assessment.
                        </p>

                    </div>
                )
            }


            {
                damageForms.map(
                    (damage, index) => (

                        <div
                            className="card"
                            key={
                                damage.returnItemId
                            }
                        >

                            <h2>
                                Damage Record
                            </h2>

                            <div className="details-grid">

                                <div>
                                    <span>
                                        Return Item ID
                                    </span>
                                    <strong>
                                        {
                                            damage.returnItemId
                                        }
                                    </strong>
                                </div>

                                <div>
                                    <span>
                                        Rental Item ID
                                    </span>
                                    <strong>
                                        {
                                            damage.equipmentRentalItemId
                                        }
                                    </strong>
                                </div>

                            </div>


                            <div className="form-group">

                                <label>
                                    Damaged Quantity
                                </label>

                                <input
                                    type="number"
                                    min="1"
                                    max={
                                        damage.maxQuantity
                                    }
                                    value={
                                        damage.damagedQuantity
                                    }
                                    onChange={(event) =>
                                        handleDamageChange(
                                            index,
                                            "damagedQuantity",
                                            event.target.value
                                        )
                                    }
                                />

                            </div>


                            <div className="form-group">

                                <label>
                                    Damage Description
                                </label>

                                <textarea
                                    rows="4"
                                    value={
                                        damage.damageDescription
                                    }
                                    onChange={(event) =>
                                        handleDamageChange(
                                            index,
                                            "damageDescription",
                                            event.target.value
                                        )
                                    }
                                />

                            </div>


                            <div className="form-group">

                                <label>
                                    Damage Level
                                </label>

                                <select
                                    value={
                                        damage.damageLevel
                                    }
                                    onChange={(event) =>
                                        handleDamageChange(
                                            index,
                                            "damageLevel",
                                            event.target.value
                                        )
                                    }
                                >

                                    <option value="MINOR">
                                        Minor
                                    </option>

                                    <option value="MODERATE">
                                        Moderate
                                    </option>

                                    <option value="SEVERE">
                                        Severe
                                    </option>

                                </select>

                            </div>


                            <div className="form-group">

                                <label>
                                    Estimated Cost
                                </label>

                                <input
                                    type="number"
                                    min="0"
                                    step="0.01"
                                    value={
                                        damage.estimatedCost
                                    }
                                    onChange={(event) =>
                                        handleDamageChange(
                                            index,
                                            "estimatedCost",
                                            event.target.value
                                        )
                                    }
                                />

                            </div>


                            <div className="form-group">

                                <label>
                                    Final Charge
                                </label>

                                <input
                                    type="number"
                                    min="0"
                                    step="0.01"
                                    value={
                                        damage.finalCharge
                                    }
                                    onChange={(event) =>
                                        handleDamageChange(
                                            index,
                                            "finalCharge",
                                            event.target.value
                                        )
                                    }
                                />

                            </div>

                        </div>
                    )
                )
            }


            {
                damageForms.length > 0 && (

                    <div className="action-row">

                        <button
                            className="secondary-btn"
                            disabled={saving}
                            onClick={() =>
                                navigate(
                                    `/returns/${returnId}`
                                )
                            }
                        >
                            Cancel
                        </button>

                        <button
                            className="primary-btn"
                            disabled={saving}
                            onClick={() => {
                                void handleSaveDamages();
                            }}
                        >
                            {
                                saving
                                    ? "Saving..."
                                    : "Save Damage Records"
                            }
                        </button>

                    </div>
                )
            }

        </div>
    );
}

export default DamageAssessment;