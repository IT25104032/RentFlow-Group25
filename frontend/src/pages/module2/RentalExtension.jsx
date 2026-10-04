import { useEffect, useState } from "react";
import {
    getActiveRentals,
    createRentalExtension,
    getRentalExtensionHistory
} from "../../services/module2/rentalExtensionService";
import "./RentalExtension.css";

const COMPANY_ID = 1000;
const USER_ID = 3;

function RentalExtension() {
    const [rentals, setRentals] = useState([]);
    const [selectedRental, setSelectedRental] = useState(null);
    const [extensionHistory, setExtensionHistory] = useState([]);

    const [searchTerm, setSearchTerm] = useState("");

    const [newDueDate, setNewDueDate] = useState("");
    const [extensionCharge, setExtensionCharge] = useState("0");
    const [reason, setReason] = useState("");

    const [loading, setLoading] = useState(true);
    const [historyLoading, setHistoryLoading] = useState(false);
    const [saving, setSaving] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    // --------------------------------------------------
    // Load active rentals
    // --------------------------------------------------
    useEffect(() => {
        loadActiveRentals();
    }, []);

    const loadActiveRentals = async () => {
        try {
            setLoading(true);
            setError("");

            const data = await getActiveRentals(COMPANY_ID);

            setRentals(data || []);
        } catch (err) {
            console.error(err);
            setError("Unable to load active rentals.");
        } finally {
            setLoading(false);
        }
    };

    // --------------------------------------------------
    // Select rental
    // --------------------------------------------------
    const handleSelectRental = async (rental) => {
        setSelectedRental(rental);
        setNewDueDate("");
        setExtensionCharge("0");
        setReason("");
        setSuccess("");
        setError("");

        try {
            setHistoryLoading(true);

            const history = await getRentalExtensionHistory(
                rental.rentalId
            );

            setExtensionHistory(history || []);
        } catch (err) {
            console.error(err);
            setExtensionHistory([]);
        } finally {
            setHistoryLoading(false);
        }
    };

    // --------------------------------------------------
    // Filter rentals
    // --------------------------------------------------
    const filteredRentals = rentals.filter((rental) => {
        const search = searchTerm.toLowerCase();

        return (
            String(rental.rentalId)
                .toLowerCase()
                .includes(search) ||
            String(rental.customerId)
                .toLowerCase()
                .includes(search) ||
            String(rental.rentalStatus)
                .toLowerCase()
                .includes(search)
        );
    });

    // --------------------------------------------------
    // Format date
    // --------------------------------------------------
    const formatDate = (date) => {
        if (!date) return "-";

        return new Date(date).toLocaleDateString("en-GB", {
            day: "2-digit",
            month: "short",
            year: "numeric"
        });
    };

    // --------------------------------------------------
    // Submit extension
    // --------------------------------------------------
    const handleSubmit = async (event) => {
        event.preventDefault();

        if (!selectedRental) {
            setError("Please select a rental first.");
            return;
        }

        if (!newDueDate) {
            setError("Please select a new due date.");
            return;
        }

        const currentDueDate = new Date(selectedRental.dueDate);
        const selectedDueDate = new Date(newDueDate);

        if (selectedDueDate <= currentDueDate) {
            setError(
                "The new due date must be after the current due date."
            );
            return;
        }

        const charge = Number(extensionCharge || 0);

        if (charge < 0) {
            setError("Extension charge cannot be negative.");
            return;
        }

        try {
            setSaving(true);
            setError("");
            setSuccess("");

            const extensionData = {
                rentalId: selectedRental.rentalId,
                companyId: COMPANY_ID,
                newDueDate: newDueDate,
                extensionCharge: charge,
                approvedBy: USER_ID,
                reason: reason.trim() || null
            };

            const response = await createRentalExtension(
                extensionData
            );

            // Update selected rental's due date immediately
            setSelectedRental((previous) => ({
                ...previous,
                dueDate: response.newDueDate
            }));

            // Refresh rental list
            await loadActiveRentals();

            // Refresh extension history
            const updatedHistory =
                await getRentalExtensionHistory(
                    selectedRental.rentalId
                );

            setExtensionHistory(updatedHistory || []);

            // Clear form
            setNewDueDate("");
            setExtensionCharge("0");
            setReason("");

            setSuccess(
                `Rental #${selectedRental.rentalId} was successfully extended.`
            );
        } catch (err) {
            console.error(err);
            setError(
                "Unable to extend the rental. Please check the details and try again."
            );
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="rental-extension-page">

            {/* ---------------------------------------- */}
            {/* PAGE HEADER */}
            {/* ---------------------------------------- */}

            <div className="module2-page-header">
                <div>
                    <h1>Extend Rental</h1>

                    <p>
                        Extend the due date of an active rental and
                        record the extension details.
                    </p>
                </div>
            </div>

            {/* ---------------------------------------- */}
            {/* ALERTS */}
            {/* ---------------------------------------- */}

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

            {/* ---------------------------------------- */}
            {/* ACTIVE RENTALS */}
            {/* ---------------------------------------- */}

            <div className="module2-card">

                <div className="module2-card-header">
                    <div>
                        <h2>Active Rentals</h2>

                        <p>
                            Select a rental to extend its due date.
                        </p>
                    </div>
                </div>

                <div className="rental-extension-search">
                    <input
                        type="text"
                        placeholder="Search by rental ID, customer ID or status..."
                        value={searchTerm}
                        onChange={(event) =>
                            setSearchTerm(event.target.value)
                        }
                    />
                </div>

                {loading ? (
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
                                <th>Rental ID</th>
                                <th>Customer ID</th>
                                <th>Start Date</th>
                                <th>Current Due Date</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                            </thead>

                            <tbody>

                            {filteredRentals.map((rental) => (
                                <tr
                                    key={rental.rentalId}
                                    className={
                                        selectedRental?.rentalId ===
                                        rental.rentalId
                                            ? "selected-row"
                                            : ""
                                    }
                                >
                                    <td>
                                        #{rental.rentalId}
                                    </td>

                                    <td>
                                        {rental.customerId}
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
                                                {rental.rentalStatus}
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
                            ))}

                            </tbody>

                        </table>

                    </div>
                )}

            </div>

            {/* ---------------------------------------- */}
            {/* EXTENSION FORM */}
            {/* ---------------------------------------- */}

            {selectedRental && (
                <>
                    <div className="module2-card">

                        <div className="module2-card-header">
                            <div>
                                <h2>
                                    Extend Rental #
                                    {selectedRental.rentalId}
                                </h2>

                                <p>
                                    Enter the revised rental due
                                    date and extension details.
                                </p>
                            </div>
                        </div>

                        <div className="rental-extension-summary">

                            <div>
                                <span>Customer ID</span>
                                <strong>
                                    {selectedRental.customerId}
                                </strong>
                            </div>

                            <div>
                                <span>Start Date</span>
                                <strong>
                                    {formatDate(
                                        selectedRental.startDate
                                    )}
                                </strong>
                            </div>

                            <div>
                                <span>Current Due Date</span>
                                <strong>
                                    {formatDate(
                                        selectedRental.dueDate
                                    )}
                                </strong>
                            </div>

                            <div>
                                <span>Status</span>
                                <strong>
                                    {selectedRental.rentalStatus}
                                </strong>
                            </div>

                        </div>

                        <form onSubmit={handleSubmit}>

                            <div className="rental-extension-form-grid">

                                {/* New Due Date */}

                                <div className="module2-form-group">
                                    <label>
                                        New Due Date
                                        <span className="required">
                                            *
                                        </span>
                                    </label>

                                    <input
                                        type="date"
                                        value={newDueDate}
                                        min={
                                            selectedRental.dueDate
                                                ? selectedRental.dueDate
                                                : undefined
                                        }
                                        onChange={(event) =>
                                            setNewDueDate(
                                                event.target.value
                                            )
                                        }
                                    />

                                    <small>
                                        Must be later than the
                                        current due date.
                                    </small>
                                </div>

                                {/* Extension Charge */}

                                <div className="module2-form-group">
                                    <label>
                                        Extension Charge
                                    </label>

                                    <input
                                        type="number"
                                        min="0"
                                        step="0.01"
                                        value={extensionCharge}
                                        onChange={(event) =>
                                            setExtensionCharge(
                                                event.target.value
                                            )
                                        }
                                    />

                                    <small>
                                        Enter 0 if no extension
                                        charge applies.
                                    </small>
                                </div>

                                {/* Approved By */}

                                <div className="module2-form-group">
                                    <label>
                                        Approved By
                                    </label>

                                    <input
                                        type="text"
                                        value={`User ${USER_ID}`}
                                        disabled
                                    />
                                </div>

                                {/* Reason */}

                                <div className="module2-form-group full-width">
                                    <label>
                                        Reason
                                    </label>

                                    <textarea
                                        rows="4"
                                        placeholder="Enter the reason for extending this rental..."
                                        value={reason}
                                        onChange={(event) =>
                                            setReason(
                                                event.target.value
                                            )
                                        }
                                    />
                                </div>

                            </div>

                            <div className="rental-extension-actions">

                                <button
                                    type="submit"
                                    className="module2-primary-button"
                                    disabled={saving}
                                >
                                    {saving
                                        ? "Extending..."
                                        : "Extend Rental"}
                                </button>

                            </div>

                        </form>

                    </div>

                    {/* ---------------------------------------- */}
                    {/* EXTENSION HISTORY */}
                    {/* ---------------------------------------- */}

                    <div className="module2-card">

                        <div className="module2-card-header">
                            <div>
                                <h2>Extension History</h2>

                                <p>
                                    Previous extensions recorded
                                    for this rental.
                                </p>
                            </div>
                        </div>

                        {historyLoading ? (
                            <div className="module2-empty-state">
                                Loading extension history...
                            </div>
                        ) : extensionHistory.length === 0 ? (
                            <div className="module2-empty-state">
                                No previous extensions recorded.
                            </div>
                        ) : (
                            <div className="module2-table-wrapper">

                                <table className="module2-table">

                                    <thead>
                                    <tr>
                                        <th>Extension ID</th>
                                        <th>Old Due Date</th>
                                        <th>New Due Date</th>
                                        <th>Charge</th>
                                        <th>Approved By</th>
                                        <th>Reason</th>
                                    </tr>
                                    </thead>

                                    <tbody>

                                    {extensionHistory.map(
                                        (extension) => (
                                            <tr
                                                key={
                                                    extension.extensionId
                                                }
                                            >
                                                <td>
                                                    #
                                                    {
                                                        extension.extensionId
                                                    }
                                                </td>

                                                <td>
                                                    {formatDate(
                                                        extension.oldDueDate
                                                    )}
                                                </td>

                                                <td>
                                                    {formatDate(
                                                        extension.newDueDate
                                                    )}
                                                </td>

                                                <td>
                                                    {Number(
                                                        extension.extensionCharge ||
                                                        0
                                                    ).toFixed(2)}
                                                </td>

                                                <td>
                                                    {
                                                        extension.approvedBy
                                                    }
                                                </td>

                                                <td>
                                                    {extension.reason ||
                                                        "-"}
                                                </td>
                                            </tr>
                                        )
                                    )}

                                    </tbody>

                                </table>

                            </div>
                        )}

                    </div>
                </>
            )}

        </div>
    );
}

export default RentalExtension;