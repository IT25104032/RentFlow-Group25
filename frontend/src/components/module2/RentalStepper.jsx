import React from "react";

function RentalStepper({ activeStep }) {

    const steps = [
        {
            number: 1,
            label: "Select Renter"
        },
        {
            number: 2,
            label: "Rental Details"
        },
        {
            number: 3,
            label: "Equipment Selection"
        },
        {
            number: 4,
            label: "Review & Confirm"
        }
    ];


    return (
        <div className="register-renter-stepper">

            {steps.map(step => (

                <div
                    key={step.number}
                    className={
                        step.number === activeStep
                            ? "register-renter-step active"
                            : "register-renter-step"
                    }
                >

                    <div className="step-number">
                        {step.number}
                    </div>

                    <span>
                        {step.label}
                    </span>

                </div>

            ))}

        </div>
    );
}

export default RentalStepper;