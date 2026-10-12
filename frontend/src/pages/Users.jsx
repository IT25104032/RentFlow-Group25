import { useCallback, useEffect, useState } from "react";
import "./Users.css";

const API = "http://localhost:8081/api/module1/users";

const emptyForm = () => ({
    full_name: "",
    email: "",
    password_hash: "",
    phone: "",
    user_role: "RENTAL_OFFICER",
    user_status: "ACTIVE"
});

async function readResponse(response) {
    const text = await response.text();
    if (!text) return null;
    try {
        return JSON.parse(text);
    } catch {
        return { message: text };
    }
}

function Users({ user }) {
    const [users, setUsers] = useState([]);
    const [showForm, setShowForm] = useState(false);
    const [editingId, setEditingId] = useState(null);
    const [message, setMessage] = useState("");
    const [messageType, setMessageType] = useState("");
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [form, setForm] = useState(emptyForm);

    const loadUsers = useCallback(async () => {
        setLoading(true);
        try {
            const response = await fetch(API, {
                method: "GET",
                credentials: "include"
            });
            const data = await readResponse(response);
            if (!response.ok) {
                throw new Error(data?.message || "Failed to load users.");
            }
            setUsers(Array.isArray(data) ? data : []);
            setMessage("");
            setMessageType("");
        } catch (error) {
            setMessage(error.message || "Failed to load users.");
            setMessageType("error");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        if (user) loadUsers();
    }, [user, loadUsers]);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setForm((previous) => ({ ...previous, [name]: value }));
    };

    const resetForm = () => {
        setForm(emptyForm());
        setEditingId(null);
        setShowForm(false);
    };

    const openCreateForm = () => {
        setForm(emptyForm());
        setEditingId(null);
        setMessage("");
        setMessageType("");
        setShowForm(true);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        if (saving) return;

        setSaving(true);
        setMessage("");
        setMessageType("");

        try {
            if (!form.full_name.trim()) {
                throw new Error("Full name is required.");
            }
            if (!form.email.trim()) {
                throw new Error("Email is required.");
            }
            if (!editingId && form.password_hash.length < 8) {
                throw new Error("Password must be at least 8 characters long.");
            }
            if (form.password_hash && form.password_hash.length < 8) {
                throw new Error("Password must be at least 8 characters long.");
            }

            const url = editingId ? `${API}/${editingId}` : API;
            const method = editingId ? "PUT" : "POST";
            const payload = {
                full_name: form.full_name.trim(),
                email: form.email.trim(),
                phone: form.phone.trim(),
                user_role: form.user_role
            };

            if (form.password_hash) payload.password_hash = form.password_hash;
            if (editingId) {
                payload.user_status = form.user_status;
            } else {
                payload.password_hash = form.password_hash;
            }

            const response = await fetch(url, {
                method,
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify(payload)
            });

            const result = await readResponse(response);
            if (!response.ok) {
                throw new Error(result?.message || "User operation failed.");
            }

            setMessage(editingId ? "User updated successfully." : "User created successfully.");
            setMessageType("success");
            resetForm();
            await loadUsers();
        } catch (error) {
            setMessage(error.message || "User operation failed.");
            setMessageType("error");
        } finally {
            setSaving(false);
        }
    };

    const editUser = (selectedUser) => {
        setForm({
            full_name: selectedUser.full_name ?? selectedUser.fullName ?? "",
            email: selectedUser.email ?? "",
            password_hash: "",
            phone: selectedUser.phone ?? "",
            user_role: selectedUser.user_role ?? selectedUser.userRole ?? "RENTAL_OFFICER",
            user_status: selectedUser.user_status ?? selectedUser.userStatus ?? "ACTIVE"
        });
        setEditingId(selectedUser.user_id ?? selectedUser.userId);
        setShowForm(true);
        setMessage("");
        setMessageType("");
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    const changeStatus = async (id, currentStatus) => {
        const newStatus = String(currentStatus).toUpperCase() === "ACTIVE"
            ? "INACTIVE"
            : "ACTIVE";

        if (!window.confirm(
            `${newStatus === "INACTIVE" ? "Deactivate" : "Activate"} this user?`
        )) return;

        setMessage("");
        setMessageType("");
        try {
            const response = await fetch(`${API}/${id}/status`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ status: newStatus })
            });
            const result = await readResponse(response);
            if (!response.ok) {
                throw new Error(result?.message || "Failed to update user status.");
            }
            setMessage(`User status changed to ${newStatus}.`);
            setMessageType("success");
            await loadUsers();
        } catch (error) {
            setMessage(error.message || "Failed to update user status.");
            setMessageType("error");
        }
    };

    const sortedUsers = [...users].sort((a, b) =>
        Number(a.user_id ?? a.userId ?? 0) - Number(b.user_id ?? b.userId ?? 0)
    );

    return (
        <div className="users-page">
            <div className="users-header">
                <div>
                    <h2>Users</h2>
                    <p>Manage staff accounts and permissions.</p>
                </div>
                <button type="button" className="add-user-button" onClick={openCreateForm}>
                    + Add User
                </button>
            </div>

            {message && (
                <div className={`users-message ${messageType}`} role="status" aria-live="polite">
                    {message}
                </div>
            )}

            {showForm && (
                <section className="users-form-card">
                    <div className="users-form-header">
                        <h3>{editingId ? "Edit User" : "Create New User"}</h3>
                        <button
                            type="button"
                            className="close-users-form"
                            onClick={resetForm}
                            aria-label="Close form"
                            disabled={saving}
                        >
                            ×
                        </button>
                    </div>

                    <form onSubmit={handleSubmit}>
                        <div className="users-form-grid">
                            <div className="users-form-group">
                                <label htmlFor="user-full-name">Full Name *</label>
                                <input
                                    id="user-full-name"
                                    name="full_name"
                                    value={form.full_name}
                                    onChange={handleChange}
                                    placeholder="Enter full name"
                                    autoComplete="name"
                                    required
                                />
                            </div>

                            <div className="users-form-group">
                                <label htmlFor="user-email">Email *</label>
                                <input
                                    id="user-email"
                                    type="email"
                                    name="email"
                                    value={form.email}
                                    onChange={handleChange}
                                    placeholder="name@example.com"
                                    autoComplete="email"
                                    required
                                />
                            </div>

                            <div className="users-form-group">
                                <label htmlFor="user-password">
                                    {editingId ? "New Password (optional)" : "Password *"}
                                </label>
                                <input
                                    id="user-password"
                                    type="password"
                                    name="password_hash"
                                    value={form.password_hash}
                                    onChange={handleChange}
                                    placeholder={editingId ? "Leave blank to keep current password" : "At least 8 characters"}
                                    minLength={8}
                                    required={!editingId}
                                    autoComplete="new-password"
                                />
                            </div>

                            <div className="users-form-group">
                                <label htmlFor="user-phone">Phone</label>
                                <input
                                    id="user-phone"
                                    type="tel"
                                    name="phone"
                                    value={form.phone}
                                    onChange={handleChange}
                                    placeholder="Enter phone number"
                                    autoComplete="tel"
                                />
                            </div>

                            <div className="users-form-group">
                                <label htmlFor="user-role">Role *</label>
                                <select id="user-role" name="user_role" value={form.user_role} onChange={handleChange} required>
                                    <option value="RENTAL_OFFICER">Rental Officer</option>
                                    <option value="COMPANY_ADMIN">Company Administrator</option>
                                </select>
                            </div>

                            {editingId && (
                                <div className="users-form-group">
                                    <label htmlFor="user-status">Status</label>
                                    <select id="user-status" name="user_status" value={form.user_status} onChange={handleChange}>
                                        <option value="ACTIVE">Active</option>
                                        <option value="INACTIVE">Inactive</option>
                                    </select>
                                </div>
                            )}
                        </div>

                        <div className="users-form-actions">
                            <button type="button" className="cancel-user-button" onClick={resetForm} disabled={saving}>
                                Cancel
                            </button>
                            <button type="submit" className="save-user-button" disabled={saving}>
                                {saving ? "Saving..." : editingId ? "Update User" : "Create User"}
                            </button>
                        </div>
                    </form>
                </section>
            )}

            <section className="users-table-card">
                <div className="users-table-header">
                    <h3>Staff Users</h3>
                    <span className="users-count">
                        {users.length} {users.length === 1 ? "user" : "users"}
                    </span>
                </div>

                {loading ? (
                    <div className="users-table-message">Loading users...</div>
                ) : (
                    <div className="users-table-wrapper">
                        <table className="users-table">
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Phone</th>
                                <th>Role</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            {sortedUsers.map((item) => {
                                const id = item.user_id ?? item.userId;
                                const status = String(item.user_status ?? item.userStatus ?? "INACTIVE").toUpperCase();
                                const fullName = item.full_name ?? item.fullName ?? "—";
                                const role = item.user_role ?? item.userRole ?? "—";

                                return (
                                    <tr key={id}>
                                        <td>{id}</td>
                                        <td><strong>{fullName}</strong></td>
                                        <td>{item.email ?? "—"}</td>
                                        <td>{item.phone || "—"}</td>
                                        <td>{role}</td>
                                        <td>
                                                <span className={`user-status ${status.toLowerCase()}`}>
                                                    {status}
                                                </span>
                                        </td>
                                        <td>
                                            <div className="users-actions">
                                                <button type="button" className="edit-user-button" onClick={() => editUser(item)}>
                                                    Edit
                                                </button>
                                                <button
                                                    type="button"
                                                    className={status === "ACTIVE" ? "deactivate-user-button" : "activate-user-button"}
                                                    onClick={() => changeStatus(id, status)}
                                                >
                                                    {status === "ACTIVE" ? "Deactivate" : "Activate"}
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                );
                            })}

                            {!loading && users.length === 0 && (
                                <tr>
                                    <td colSpan="7" className="empty-users">
                                        No users found for this company.
                                    </td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>
        </div>
    );
}

export default Users;
