
import { useEffect, useMemo, useState } from "react";
import {
    getActiveRentalsForIssue,
    issueRental,
    cancelRental,
    getRentalItemsForIssue
} from "../../services/module2/equipmentIssueService";
import "./IssueEquipment.css";


function getLocalDateString() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}

function getLoggedInUser() {
    try {
        return JSON.parse(sessionStorage.getItem("rentflowUser") || "null");
    } catch {
        return null;
    }
}

function IssueEquipment() {
    const loggedInUser = getLoggedInUser();
    const COMPANY_ID = loggedInUser?.companyId ?? null;

    const [rentals, setRentals] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [loadingRentals, setLoadingRentals] = useState(true);
    const [actionRentalId, setActionRentalId] = useState(null);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [pendingRental, setPendingRental] = useState(null);
    const [pendingItems, setPendingItems] = useState([]);
    const [loadingItems, setLoadingItems] = useState(false);
    const [today, setToday] = useState(getLocalDateString());

    useEffect(() => {
        const interval = window.setInterval(() => {
            setToday(getLocalDateString());
        }, 15000);

        return () => window.clearInterval(interval);
    }, []);

    useEffect(() => {
        loadRentalsForIssue();
    }, []);

    async function loadRentalsForIssue() {
        try {
            setLoadingRentals(true);
            setError("");

            const data = await getActiveRentalsForIssue(COMPANY_ID);
            setRentals(data || []);
        } catch (err) {
            console.error(err);
            setError("Unable to load draft rentals available for issue.");
        } finally {
            setLoadingRentals(false);
        }
    }

    async function handleIssueRental(rental) {
        setError("");
        setSuccess("");
        setLoadingItems(true);
        setActionRentalId(rental.rentalId);

        try {
            const items = await getRentalItemsForIssue(rental.rentalId);

            if (!Array.isArray(items) || items.length === 0) {
                throw new Error("No equipment items were found for this rental.");
            }

            setPendingItems(items);
            setPendingRental(rental);
        } catch (err) {
            console.error(err);
            setError(err.message || "Unable to load rental equipment.");
        } finally {
            setLoadingItems(false);
            setActionRentalId(null);
        }
    }

    async function confirmIssueRental() {
        if (!pendingRental || actionRentalId !== null) return;

        const rental = pendingRental;

        try {
            setActionRentalId(rental.rentalId);
            setError("");
            setSuccess("");

            await issueRental(rental.rentalId, COMPANY_ID);

            setPendingRental(null);
            setPendingItems([]);

            setSuccess(
                `Rental #${rental.rentalId} was successfully issued. The rental is now ACTIVE.`
            );

            setRentals(previous =>
                previous.filter(item => item.rentalId !== rental.rentalId)
            );
        } catch (err) {
            console.error(err);
            setError(err.message || `Unable to issue rental #${rental.rentalId}.`);
        } finally {
            setActionRentalId(null);
        }
    }

    async function handleCancelRental(rental) {
        const confirmed = window.confirm(
            `Cancel rental #${rental.rentalId}?\n\nThis rental will be kept in the database and Rental History, but it will no longer participate in the rental workflow.`
        );

        if (!confirmed) return;

        try {
            setActionRentalId(rental.rentalId);
            setError("");
            setSuccess("");

            await cancelRental(rental.rentalId, COMPANY_ID);

            setSuccess(
                `Rental #${rental.rentalId} was cancelled and will remain available in Rental History.`
            );

            setRentals(previous =>
                previous.filter(item => item.rentalId !== rental.rentalId)
            );
        } catch (err) {
            console.error(err);
            setError(`Unable to cancel rental #${rental.rentalId}.`);
        } finally {
            setActionRentalId(null);
        }
    }

    const filteredRentals = useMemo(() => {
        const search = searchTerm.trim().toLowerCase();

        if (!search) return rentals;

        return rentals.filter(rental =>
            String(rental.rentalId).toLowerCase().includes(search) ||
            String(rental.customerId).toLowerCase().includes(search) ||
            String(rental.rentalStatus).toLowerCase().includes(search)
        );
    }, [rentals, searchTerm]);

    function formatDate(date) {
        if (!date) return "-";

        return new Date(date).toLocaleDateString("en-GB", {
            day: "2-digit",
            month: "short",
            year: "numeric"
        });
    }

    return (
        <div className="issue-equipment-page">
            <div className="module2-page-header">
                <div>
                    <h1>Issue Equipment</h1>
                    <p>
                        Issue equipment for draft rentals or cancel a rental
                        before it begins.
                    </p>
                </div>
            </div>

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

            <div className="module2-card">
                <div className="module2-card-header">
                    <div>
                        <h2>Draft Rentals</h2>
                        <p>
                            All rentals currently in DRAFT status are shown here.
                            A cancelled rental is removed from this operational
                            list but remains in Rental History.
                        </p>
                    </div>
                </div>

                <div className="issue-equipment-search">
                    <input
                        type="text"
                        placeholder="Search by rental ID or customer ID..."
                        value={searchTerm}
                        onChange={event => setSearchTerm(event.target.value)}
                    />
                </div>

                {loadingRentals ? (
                    <div className="module2-empty-state">
                        Loading draft rentals...
                    </div>
                ) : filteredRentals.length === 0 ? (
                    <div className="module2-empty-state">
                        No draft rentals found.
                    </div>
                ) : (
                    <div className="module2-table-wrapper">
                        <table className="module2-table">
                            <thead>
                            <tr>
                                <th>Rental ID</th>
                                <th>Customer ID</th>
                                <th>Rental Date</th>
                                <th>Start Date</th>
                                <th>Due Date</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                            </thead>

                            <tbody>
                            {filteredRentals.map(rental => {
                                const isWorking =
                                    actionRentalId === rental.rentalId;

                                const startDate = rental.startDate
                                    ? String(rental.startDate).slice(0, 10)
                                    : "";

                                const isBeforeStartDate =
                                    Boolean(startDate) && startDate > today;

                                const isIssueDisabled =
                                    actionRentalId !== null ||
                                    loadingItems ||
                                    isBeforeStartDate;

                                return (
                                    <tr key={rental.rentalId}>
                                        <td>#{rental.rentalId}</td>
                                        <td>#{rental.customerId}</td>
                                        <td>{formatDate(rental.rentalDate)}</td>
                                        <td>{formatDate(rental.startDate)}</td>
                                        <td>{formatDate(rental.dueDate)}</td>
                                        <td>
                                            <span className="rental-status-badge draft">
                                              {rental.rentalStatus}
                                            </span>
                                        </td>
                                        <td>
                                            <div className="issue-action-group">
                                                <button
                                                    type="button"
                                                    className="module2-primary-button"
                                                    disabled={isIssueDisabled}
                                                    title={
                                                        isBeforeStartDate
                                                            ? `Issuing is available from ${formatDate(startDate)}.`
                                                            : "Review equipment for this rental"
                                                    }
                                                    onClick={() => handleIssueRental(rental)}
                                                >
                                                    {isWorking
                                                        ? "Processing..."
                                                        : "Issue"}
                                                </button>

                                                <button
                                                    type="button"
                                                    className="module2-danger-button"
                                                    disabled={
                                                        actionRentalId !== null || loadingItems
                                                    }
                                                    onClick={() => handleCancelRental(rental)}
                                                >
                                                    Cancel
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                );
                            })}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {pendingRental && (
                <div
                    role="presentation"
                    className="issue-review-overlay"
                >
                    <div
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="issue-review-title"
                        className="issue-review-dialog"
                    >
                        <h2 id="issue-review-title">
                            Confirm equipment handover
                        </h2>

                        <p>
                            Rental #{pendingRental.rentalId} · Customer #
                            {pendingRental.customerId}
                        </p>

                        <p>
                            Review the equipment and quantities before issuing.
                            This operation issues the{" "}
                            <strong>entire requested quantity</strong>{" "}
                            for every item.
                        </p>

                        <div className="module2-table-wrapper">
                            <table className="module2-table">
                                <thead>
                                <tr>
                                    <th>Equipment ID</th>
                                    <th>Requested / to issue</th>
                                    <th>Item status</th>
                                </tr>
                                </thead>

                                <tbody>
                                {pendingItems.map(item => (
                                    <tr key={item.rentalItemId}>
                                        <td>#{item.equipmentId}</td>
                                        <td>{item.quantity}</td>
                                        <td>{item.itemStatus}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                        <p className="issue-review-note">
                            Current inventory stock is not revalidated by this
                            screen. Confirm availability with the equipment
                            module before physical handover.
                        </p>

                        <div className="issue-review-actions">
                            <button
                                type="button"
                                className="issue-review-back-button"
                                disabled={actionRentalId !== null}
                                onClick={() => {
                                    setPendingRental(null);
                                    setPendingItems([]);
                                }}
                            >
                                Back
                            </button>

                            <button
                                type="button"
                                className="module2-primary-button"
                                disabled={
                                    actionRentalId !== null ||
                                    pendingItems.some(
                                        item =>
                                            item.itemStatus !== "SELECTED" ||
                                            !(item.quantity > 0)
                                    )
                                }
                                onClick={confirmIssueRental}
                            >
                                {actionRentalId !== null
                                    ? "Processing..."
                                    : "Confirm issue all equipment"}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default IssueEquipment;
