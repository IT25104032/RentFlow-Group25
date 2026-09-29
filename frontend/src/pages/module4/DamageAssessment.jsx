import { useLocation, useNavigate, useParams } from "react-router-dom";
import { useState } from "react";

function DamageAssessment() {

    const { returnId } = useParams();
    const location = useLocation();
    const navigate = useNavigate();

    const damagedItems =
        location.state?.damagedItems || [];

    const [damageForms, setDamageForms] =
        useState(
            damagedItems.map((item) => ({
                returnItemId: item.returnItemId,
                damagedQuantity: item.quantityReturned,
                damageDescription: "",
                damageLevel: "MINOR",
                estimatedCost: 0,
                finalCharge: 0,
                status: "ASSESSED"
            }))
        );

    function handleDamageChange(index, field, value) {

        setDamageForms((previous) => {

            const updated = [...previous];

            updated[index] = {
                ...updated[index],
                [field]: value
            };

            return updated;
        });
    }

    return (

        <div className="module-page">

            <h1>Damage Assessment</h1>

            <p>
                Return #{returnId}
            </p>

            {damageForms.map((damage, index) => (

                <div
                    className="card"
                    key={damage.returnItemId}
                >

                    <h2>
                        Damage Record
                    </h2>

                    <p>
                        Return Item ID:
                        {" "}
                        {damage.returnItemId}
                    </p>



                    <label>
                        Damaged Quantity
                    </label>

                    <input
                        type="number"
                        value={damage.damagedQuantity}
                        onChange={(e) =>
                            handleDamageChange(
                                index,
                                "damagedQuantity",
                                e.target.value
                            )
                        }
                    />


                    <label>
                        Damage Description
                    </label>

                    <textarea
                        value={damage.damageDescription}
                        onChange={(e) =>
                            handleDamageChange(
                                index,
                                "damageDescription",
                                e.target.value
                            )
                        }
                    />


                    <label>
                        Damage Level
                    </label>

                    <select
                        value={damage.damageLevel}
                        onChange={(e) =>
                            handleDamageChange(
                                index,
                                "damageLevel",
                                e.target.value
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


                    <label>
                        Estimated Cost
                    </label>

                    <input
                        type="number"
                        value={damage.estimatedCost}
                        onChange={(e) =>
                            handleDamageChange(
                                index,
                                "estimatedCost",
                                e.target.value
                            )
                        }
                    />


                    <label>
                        Final Charge
                    </label>

                    <input
                        type="number"
                        value={damage.finalCharge}
                        onChange={(e) =>
                            handleDamageChange(
                                index,
                                "finalCharge",
                                e.target.value
                            )
                        }
                    />

                </div>

            ))}

        </div>
    );

    async function handleSaveDamages() {

        try {

            for (const damage of damageForms) {

                const damageData = {

                    returnItem: {
                        returnItemId:
                        damage.returnItemId
                    },

                    damagedQuantity:
                        Number(damage.damagedQuantity),

                    damageDescription:
                    damage.damageDescription,

                    damageLevel:
                    damage.damageLevel,

                    estimatedCost:
                        Number(damage.estimatedCost),

                    finalCharge:
                        Number(damage.finalCharge),

                    assessedBy:
                    CURRENT_USER_ID,

                    assessmentDate:
                        new Date().toISOString(),

                    status:
                    damage.status
                };

                await createDamageRecord(damageData);
            }

            navigate(`/returns/${returnId}`);

        } catch (err) {

            console.error(err);
        }
    }

}

export default DamageAssessment;
