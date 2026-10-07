import { useEffect, useState } from 'react'
import './CompanyManagement.css'

const API_URL = 'http://localhost:8081/api/module1/companies'

function CompanyManagement({ user }) {
    const [companies, setCompanies] = useState([])
    const [showForm, setShowForm] = useState(true)
    const [editingId, setEditingId] = useState(null)
    const [message, setMessage] = useState('')
    const [messageType, setMessageType] = useState('')

    const [form, setForm] = useState({
        companyName: '',
        registrationNo: '',
        email: '',
        phone: '',
        address: '',
        adminFullName: '',
        adminPassword: '',
        status: 'ACTIVE'
    })

    const loadCompanies = async () => {
        try {
            const response = await fetch(API_URL, {
                method: 'GET',
                credentials: 'include'
            })

            if (!response.ok) {
                const data = await response.json().catch(() => ({}))

                throw new Error(
                    data.message || 'Failed to fetch companies'
                )
            }

            const data = await response.json()
            setCompanies(data)

        } catch (error) {
            setMessage(error.message)
            setMessageType('error')
        }
    }

    useEffect(() => {
        if (user) {
            loadCompanies()
        }
    }, [user])

    const handleChange = (e) => {
        const { name, value } = e.target

        setForm((previous) => ({
            ...previous,
            [name]: value
        }))
    }

    const resetForm = () => {
        setForm({
            companyName: '',
            registrationNo: '',
            email: '',
            phone: '',
            address: '',
            adminFullName: '',
            adminPassword: '',
            status: 'ACTIVE'
        })

        setEditingId(null)
    }

    const handleEdit = (company) => {
        const id =
            company.company_id ??
            company.companyId

        const companyName =
            company.company_name ??
            company.companyName

        const registrationNo =
            company.registration_no ??
            company.registrationNo

        const status =
            company.company_status ??
            company.companyStatus

        setForm({
            companyName: companyName || '',
            registrationNo: registrationNo || '',
            email: company.email || '',
            phone: company.phone || '',
            address: company.address || '',
            adminFullName: '',
            adminPassword: '',
            status: status || 'ACTIVE'
        })

        setEditingId(id)
        setShowForm(true)
        setMessage('')
        setMessageType('')

        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        })
    }

    const handleSubmit = async (e) => {
        e.preventDefault()

        setMessage('')
        setMessageType('')

        try {
            let response

            if (editingId) {
                const companyData = {
                    company_name: form.companyName,
                    registration_no: form.registrationNo,
                    email: form.email,
                    phone: form.phone,
                    address: form.address,
                    company_status: form.status
                }

                response = await fetch(
                    `${API_URL}/${editingId}`,
                    {
                        method: 'PUT',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        credentials: 'include',
                        body: JSON.stringify(companyData)
                    }
                )
            } else {
                const companyData = {
                    companyName: form.companyName,
                    registrationNo: form.registrationNo,
                    email: form.email,
                    phone: form.phone,
                    address: form.address,
                    adminFullName: form.adminFullName,
                    adminPassword: form.adminPassword
                }

                response = await fetch(
                    API_URL,
                    {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        credentials: 'include',
                        body: JSON.stringify(companyData)
                    }
                )
            }

            const data = await response
                .json()
                .catch(() => ({}))

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    (
                        editingId
                            ? 'Failed to update company'
                            : 'Failed to register company'
                    )
                )
            }

            if (editingId) {
                setMessage(
                    'Company updated successfully'
                )
            } else {
                setMessage(
                    `Company registered successfully. Company Admin username: ${data.username}`
                )
            }

            setMessageType('success')

            resetForm()

            await loadCompanies()

        } catch (error) {
            setMessage(error.message)
            setMessageType('error')
        }
    }

    const handleDelete = async (id) => {
        const confirmed = window.confirm(
            'Are you sure you want to deactivate this company?'
        )

        if (!confirmed) {
            return
        }

        try {
            const response = await fetch(
                `${API_URL}/${id}`,
                {
                    method: 'DELETE',
                    credentials: 'include'
                }
            )

            const data = await response
                .json()
                .catch(() => ({}))

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    'Failed to deactivate company'
                )
            }

            setMessage(
                'Company deactivated successfully'
            )

            setMessageType('success')

            await loadCompanies()

        } catch (error) {
            setMessage(error.message)
            setMessageType('error')
        }
    }

    return (
        <div className="company-page">

            <div className="section-header">

                <div>
                    <h2>Company Management</h2>

                    <p>
                        Register and manage rental companies.
                    </p>
                </div>

                <button
                    className="primary-button"
                    onClick={() => {
                        resetForm()
                        setShowForm(true)
                        setMessage('')
                        setMessageType('')
                    }}
                >
                    + Add Company
                </button>

            </div>

            {message && (
                <div
                    className={`company-message ${messageType}`}
                >
                    {message}
                </div>
            )}

            {showForm && (
                <div className="company-form-card">

                    <h3>
                        {editingId
                            ? 'Edit Company'
                            : 'Register New Company'}
                    </h3>

                    <form onSubmit={handleSubmit}>

                        <div className="form-grid">

                            <div className="form-group">
                                <label>
                                    Company Name <span>*</span>
                                </label>

                                <input
                                    type="text"
                                    name="companyName"
                                    value={form.companyName}
                                    onChange={handleChange}
                                    placeholder="Enter company name"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>
                                    Registration No
                                </label>

                                <input
                                    type="text"
                                    name="registrationNo"
                                    value={form.registrationNo}
                                    onChange={handleChange}
                                    placeholder="Enter registration number"
                                />
                            </div>

                            <div className="form-group">
                                <label>
                                    Email <span>*</span>
                                </label>

                                <input
                                    type="email"
                                    name="email"
                                    value={form.email}
                                    onChange={handleChange}
                                    placeholder="Enter company email"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>
                                    Phone
                                </label>

                                <input
                                    type="text"
                                    name="phone"
                                    value={form.phone}
                                    onChange={handleChange}
                                    placeholder="Enter phone number"
                                />
                            </div>

                            <div className="form-group full-width">
                                <label>
                                    Address
                                </label>

                                <textarea
                                    name="address"
                                    value={form.address}
                                    onChange={handleChange}
                                    placeholder="Enter company address"
                                    rows="4"
                                />
                            </div>

                        </div>

                        {!editingId && (
                            <>
                                <h3 className="admin-section-title">
                                    Initial Company Administrator
                                </h3>

                                <div className="form-grid">

                                    <div className="form-group">
                                        <label>
                                            Admin Full Name <span>*</span>
                                        </label>

                                        <input
                                            type="text"
                                            name="adminFullName"
                                            value={form.adminFullName}
                                            onChange={handleChange}
                                            placeholder="Enter administrator name"
                                            required
                                        />
                                    </div>

                                    <div className="form-group">
                                        <label>
                                            Admin Password <span>*</span>
                                        </label>

                                        <input
                                            type="password"
                                            name="adminPassword"
                                            value={form.adminPassword}
                                            onChange={handleChange}
                                            placeholder="Enter initial password"
                                            required
                                        />
                                    </div>

                                </div>

                                <p className="admin-login-note">
                                    The company email will be used as the
                                    Company Administrator username.
                                </p>
                            </>
                        )}

                        <div className="form-group status-group">
                            <label>
                                Status
                            </label>

                            <select
                                name="status"
                                value={form.status}
                                onChange={handleChange}
                            >
                                <option value="ACTIVE">
                                    ACTIVE
                                </option>

                                <option value="INACTIVE">
                                    INACTIVE
                                </option>

                                <option value="SUSPENDED">
                                    SUSPENDED
                                </option>
                            </select>
                        </div>

                        <div className="form-actions">

                            <button
                                type="submit"
                                className="primary-button"
                            >
                                {editingId
                                    ? 'Update Company'
                                    : 'Save Company'}
                            </button>

                            <button
                                type="button"
                                className="secondary-button"
                                onClick={() => {
                                    resetForm()
                                    setShowForm(false)
                                    setMessage('')
                                    setMessageType('')
                                }}
                            >
                                Cancel
                            </button>

                        </div>

                    </form>

                </div>
            )}

            <div className="companies-table-card">

                <div className="table-header">

                    <h3>
                        Registered Companies
                    </h3>

                    <span>
                        {companies.length} companies
                    </span>

                </div>

                <div className="table-wrapper">

                    <table>

                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Registration No</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Address</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                        </thead>

                        <tbody>

                        {companies.length === 0 ? (
                            <tr>
                                <td
                                    colSpan="8"
                                    className="empty-row"
                                >
                                    No companies registered yet.
                                </td>
                            </tr>
                        ) : (
                            companies.map((company) => {

                                const id =
                                    company.company_id ??
                                    company.companyId

                                const name =
                                    company.company_name ??
                                    company.companyName

                                const registrationNo =
                                    company.registration_no ??
                                    company.registrationNo

                                const status =
                                    company.company_status ??
                                    company.companyStatus

                                return (
                                    <tr key={id}>

                                        <td>
                                            {id}
                                        </td>

                                        <td>
                                            {name}
                                        </td>

                                        <td>
                                            {registrationNo || '-'}
                                        </td>

                                        <td>
                                            {company.email}
                                        </td>

                                        <td>
                                            {company.phone || '-'}
                                        </td>

                                        <td>
                                            {company.address || '-'}
                                        </td>

                                        <td>
                                            <span
                                                className="status-badge"
                                            >
                                                {status}
                                            </span>
                                        </td>

                                        <td>
                                            <div
                                                style={{
                                                    display: 'flex',
                                                    gap: '8px'
                                                }}
                                            >

                                                <button
                                                    type="button"
                                                    className="edit-button"
                                                    onClick={() =>
                                                        handleEdit(company)
                                                    }
                                                >
                                                    Edit
                                                </button>

                                                <button
                                                    type="button"
                                                    className="delete-button"
                                                    onClick={() =>
                                                        handleDelete(id)
                                                    }
                                                >
                                                    Deactivate
                                                </button>

                                            </div>
                                        </td>

                                    </tr>
                                )
                            })
                        )}

                        </tbody>

                    </table>

                </div>

            </div>

        </div>
    )
}

export default CompanyManagement