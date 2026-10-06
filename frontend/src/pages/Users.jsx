import { useEffect, useState } from "react";

const API = "http://localhost:8081/api/module1/users";

function Users({ user }) {
    const [users, setUsers] = useState([]);
    const [showForm, setShowForm] = useState(false);
    const [editingId, setEditingId] = useState(null);
    const [message, setMessage] = useState("");

    const [form, setForm] = useState({
        full_name: "",
        email: "",
        password_hash: "",
        phone: "",
        user_role: "RENTAL_OFFICER",
        user_status: "ACTIVE",
        company: {
            company_id: 1000
        }
    });

    useEffect(() => {
        loadUsers();
    }, [user]);

    const loadUsers = async () => {
        try {
            if (!user?.companyId) {
                setMessage("Company information is missing");
                return;
            }

            const response = await fetch(
                `${API}/company/${user.companyId}`
            );

            if (!response.ok) {
                throw new Error("Failed to load users");
            }

            const data = await response.json();
            setUsers(data);

        } catch (error) {
            setMessage(error.message);
        }
    };

    const handleChange = (e) => {
        setForm({
            ...form,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        setMessage("");

        try {
            const url = editingId
                ? `${API}/${editingId}`
                : API;

            const method = editingId ? "PUT" : "POST";

            const response = await fetch(url, {
                method,
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(form)
            });

            const result = await response.text();

            if (!response.ok) {
                throw new Error(result || "Operation failed");
            }

            setMessage(
                editingId
                    ? "User updated successfully"
                    : "User created successfully"
            );

            resetForm();
            loadUsers();

        } catch (error) {
            setMessage(error.message);
        }
    };

    const resetForm = () => {
        setForm({
            full_name: "",
            email: "",
            password_hash: "",
            phone: "",
            user_role: "RENTAL_OFFICER",
            user_status: "ACTIVE",
            company: {
                company_id: user?.companyId
            }
        });

        setEditingId(null);
        setShowForm(false);
    };

    const editUser = (user) => {
        setForm({
            full_name: user.full_name || "",
            email: user.email || "",
            password_hash: "",
            phone: user.phone || "",
            user_role: user.user_role || "RENTAL_OFFICER",
            user_status: user.user_status || "ACTIVE",
            company: user.company || {
                company_id: 1000
            }
        });

        setEditingId(user.user_id);
        setShowForm(true);
    };

    const deleteUser = async (id) => {
        if (!window.confirm("Delete this user?")) {
            return;
        }

        try {
            const response = await fetch(`${API}/${id}`, {
                method: "DELETE"
            });

            if (!response.ok) {
                throw new Error("Failed to delete user");
            }

            setMessage("User deleted successfully");
            loadUsers();

        } catch (error) {
            setMessage(error.message);
        }
    };

    const changeStatus = async (id, status) => {
        try {
            const response = await fetch(
                `${API}/${id}/status?status=${status}`,
                {
                    method: "PATCH"
                }
            );

            if (!response.ok) {
                throw new Error("Failed to update status");
            }

            loadUsers();

        } catch (error) {
            setMessage(error.message);
        }
    };

    return (
        <div className="page">

            <div className="page-header">
                <div>
                    <h1>Users</h1>
                    <p>Manage staff accounts and permissions.</p>
                </div>

                <button
                    className="primary-button"
                    onClick={() => {
                        resetForm();
                        setShowForm(true);
                    }}
                >
                    + Add User
                </button>
            </div>

            {message && (
                <div className="message">
                    {message}
                </div>
            )}

            {showForm && (
                <div className="form-card">

                    <h2>
                        {editingId
                            ? "Edit User"
                            : "Create New User"}
                    </h2>

                    <form onSubmit={handleSubmit}>

                        <div className="form-grid">

                            <div>
                                <label>Full Name *</label>
                                <input
                                    name="full_name"
                                    value={form.full_name}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div>
                                <label>Email *</label>
                                <input
                                    type="email"
                                    name="email"
                                    value={form.email}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div>
                                <label>
                                    {editingId
                                        ? "New Password"
                                        : "Password *"}
                                </label>

                                <input
                                    type="password"
                                    name="password_hash"
                                    value={form.password_hash}
                                    onChange={handleChange}
                                    required={!editingId}
                                />
                            </div>

                            <div>
                                <label>Phone</label>
                                <input
                                    name="phone"
                                    value={form.phone}
                                    onChange={handleChange}
                                />
                            </div>

                            <div>
                                <label>Role *</label>

                                <select
                                    name="user_role"
                                    value={form.user_role}
                                    onChange={handleChange}
                                >
                                    <option value="COMPANY_ADMIN">
                                        Company Administrator
                                    </option>

                                    <option value="RENTAL_OFFICER">
                                        Rental Officer
                                    </option>
                                </select>
                            </div>

                            <div>
                                <label>Status</label>

                                <select
                                    name="user_status"
                                    value={form.user_status}
                                    onChange={handleChange}
                                >
                                    <option value="ACTIVE">
                                        Active
                                    </option>

                                    <option value="INACTIVE">
                                        Inactive
                                    </option>
                                </select>
                            </div>

                        </div>

                        <div className="form-actions">

                            <button
                                type="submit"
                                className="primary-button"
                            >
                                {editingId
                                    ? "Update User"
                                    : "Create User"}
                            </button>

                            <button
                                type="button"
                                className="secondary-button"
                                onClick={resetForm}
                            >
                                Cancel
                            </button>

                        </div>

                    </form>
                </div>
            )}

            <div className="table-card">

                <h2>Staff Users</h2>

                <table>

                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Role</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                    </thead>

                    <tbody>

                    {users.map((user) => (

                        <tr key={user.user_id}>

                            <td>
                                {user.user_id}
                            </td>

                            <td>
                                {user.full_name}
                            </td>

                            <td>
                                {user.email}
                            </td>

                            <td>
                                {user.phone}
                            </td>

                            <td>
                                {user.user_role}
                            </td>

                            <td>
                                {user.user_status}
                            </td>

                            <td>

                                <button
                                    onClick={() =>
                                        editUser(user)
                                    }
                                >
                                    Edit
                                </button>

                                <button
                                    onClick={() =>
                                        changeStatus(
                                            user.user_id,
                                            user.user_status === "ACTIVE"
                                                ? "INACTIVE"
                                                : "ACTIVE"
                                        )
                                    }
                                >
                                    {user.user_status === "ACTIVE"
                                        ? "Deactivate"
                                        : "Activate"}
                                </button>

                                <button
                                    onClick={() =>
                                        deleteUser(
                                            user.user_id
                                        )
                                    }
                                >
                                    Delete
                                </button>

                            </td>

                        </tr>

                    ))}

                    </tbody>

                </table>

            </div>

        </div>
    );
}

export default Users;