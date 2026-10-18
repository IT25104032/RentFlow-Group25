import { useState } from "react";
import "./ChangePassword.css";

const API_URL = "http://localhost:8081/api/auth/change-password";

function VisibilityIcon({ visible }) {
    return visible ? (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="M3 3l18 18" />
            <path d="M10.6 10.6a2 2 0 0 0 2.8 2.8" />
            <path d="M9.9 5.2A10.7 10.7 0 0 1 12 5c5 0 9 4 10 7a13.4 13.4 0 0 1-3.3 4.4" />
            <path d="M6.5 6.5A13.5 13.5 0 0 0 2 12c1 3 5 7 10 7a10.8 10.8 0 0 0 4.1-.8" />
        </svg>
    ) : (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12z" />
            <circle cx="12" cy="12" r="3" />
        </svg>
    );
}

function ChangePassword({ user }) {
    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [message, setMessage] = useState("");
    const [messageType, setMessageType] = useState("");
    const [loading, setLoading] = useState(false);
    const [showCurrentPassword, setShowCurrentPassword] = useState(false);
    const [showNewPassword, setShowNewPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    const handleSubmit = async (event) => {
        event.preventDefault();
        setMessage("");
        setMessageType("");

        if (newPassword !== confirmPassword) {
            setMessage("New password and confirmation do not match.");
            setMessageType("error");
            return;
        }

        if (newPassword.length < 8) {
            setMessage("New password must be at least 8 characters long.");
            setMessageType("error");
            return;
        }

        if (newPassword.length > 72) {
            setMessage("New password must not exceed 72 characters.");
            setMessageType("error");
            return;
        }

        setLoading(true);
        try {
            const response = await fetch(API_URL, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                credentials: "include",
                body: JSON.stringify({
                    currentPassword,
                    newPassword,
                    confirmPassword
                })
            });

            let data = {};
            try {
                data = await response.json();
            } catch {
                data = {};
            }

            if (!response.ok) {
                throw new Error(data.message || "Unable to change password.");
            }

            setMessage(data.message || "Password changed successfully.");
            setMessageType("success");
            setCurrentPassword("");
            setNewPassword("");
            setConfirmPassword("");
        } catch (error) {
            setMessage(error.message || "Unable to change password. Please try again.");
            setMessageType("error");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="change-password-page">
            <section className="change-password-card">
                <div className="change-password-heading">
                    <div className="change-password-icon" aria-hidden="true">🔑</div>
                    <div>
                        <h2>Change Password</h2>
                        <p>Update the password for your RentFlow account.</p>
                    </div>
                </div>

                <div className="change-password-account">
                    <span>Signed in as</span>
                    <strong>{user?.email || "Current account"}</strong>
                </div>

                <form onSubmit={handleSubmit} className="change-password-form">
                    <label htmlFor="current-password">Current password</label>
                    <div className="change-password-input-wrap">
                        <input
                            id="current-password"
                            type={showCurrentPassword ? "text" : "password"}
                            autoComplete="current-password"
                            value={currentPassword}
                            onChange={(event) => setCurrentPassword(event.target.value)}
                            required
                        />
                        <button
                            type="button"
                            className="change-password-visibility"
                            onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                            aria-label={showCurrentPassword ? "Hide current password" : "Show current password"}
                            aria-pressed={showCurrentPassword}
                            title={showCurrentPassword ? "Hide password" : "Show password"}
                        >
                            <VisibilityIcon visible={showCurrentPassword} />
                        </button>
                    </div>

                    <label htmlFor="new-password">New password</label>
                    <div className="change-password-input-wrap">
                        <input
                            id="new-password"
                            type={showNewPassword ? "text" : "password"}
                            autoComplete="new-password"
                            minLength={8}
                            maxLength={72}
                            value={newPassword}
                            onChange={(event) => setNewPassword(event.target.value)}
                            required
                        />
                        <button
                            type="button"
                            className="change-password-visibility"
                            onClick={() => setShowNewPassword(!showNewPassword)}
                            aria-label={showNewPassword ? "Hide new password" : "Show new password"}
                            aria-pressed={showNewPassword}
                            title={showNewPassword ? "Hide password" : "Show password"}
                        >
                            <VisibilityIcon visible={showNewPassword} />
                        </button>
                    </div>
                    <small className="change-password-hint">Use at least 8 characters.</small>

                    <label htmlFor="confirm-password">Confirm new password</label>
                    <div className="change-password-input-wrap">
                        <input
                            id="confirm-password"
                            type={showConfirmPassword ? "text" : "password"}
                            autoComplete="new-password"
                            minLength={8}
                            maxLength={72}
                            value={confirmPassword}
                            onChange={(event) => setConfirmPassword(event.target.value)}
                            required
                        />
                        <button
                            type="button"
                            className="change-password-visibility"
                            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                            aria-label={showConfirmPassword ? "Hide confirm password" : "Show confirm password"}
                            aria-pressed={showConfirmPassword}
                            title={showConfirmPassword ? "Hide password" : "Show password"}
                        >
                            <VisibilityIcon visible={showConfirmPassword} />
                        </button>
                    </div>

                    {message && (
                        <div
                            className={`change-password-message ${messageType}`}
                            role={messageType === "error" ? "alert" : "status"}
                        >
                            {message}
                        </div>
                    )}

                    <button type="submit" disabled={loading}>
                        {loading ? "Updating password..." : "Update Password"}
                    </button>
                </form>
            </section>
        </div>
    );
}

export default ChangePassword;
