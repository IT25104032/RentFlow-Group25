import { useEffect, useState } from 'react'
import './CompanyManagement.css'

const API_URL = 'http://localhost:8081/api/module1/companies'

function CompanyManagement({ user }) {
    const [companies, setCompanies] = useState([])
    const [showForm, setShowForm] = useState(true)
    const [message, setMessage] = useState('')
    const [messageType, setMessageType] = useState('')

    const [form, setForm] = useState({
        companyName: '',
        registrationNo: '',
        email: '',
        phone: '',
        address: '',
        registeredByUserId: user?.userId || 1,
        status: 'ACTIVE'
    })

    useEffect(() => {
        if (user?.userId) {
            setForm((previous) => ({
                ...previous,
                registeredByUserId: user.userId
            }))
        }
    }, [user])

    const loadCompanies = async () => {
        try {
            const response = await fetch(API_URL)

            if (!response.ok) {
                throw new Error('Failed to fetch companies')
            }

            const data = await response.json()
            setCompanies(data)
        } catch (error) {
            setMessage(error.message)
            setMessageType('error')
        }
    }

    useEffect(() => {
        loadCompanies()
    }, [])

    const handleChange = (e) => {
        const { name, value } = e.target

        setForm((previous) => ({
            ...previous,
            [name]: value
        }))
    }

    const handleSubmit = async (e) => {
        e.preventDefault()

        setMessage('')
        setMessageType('')

        const companyData = {
            company_name: form.companyName,
            registration_no: form.registrationNo,
            email: form.email,
            phone: form.phone,
            address: form.address,
            registered_by: {
                user_id: Number(form.registeredByUserId)
            },
            company_status: form.status
        }

        try {
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(companyData)
            })

            const data = await response.json()

            if (!response.ok) {
                throw new Error(
                    data.message || 'Failed to create company'
                )
            }

            setMessage('Company created successfully')
            setMessageType('success')

            setForm({
                companyName: '',
                registrationNo: '',
                email: '',
                phone: '',
                address: '',
                registeredByUserId: user?.userId || 1,
                status: 'ACTIVE'
            })

            await loadCompanies()

        } catch (error) {
            setMessage(error.message)
            setMessageType('error')
        }
    }

    const handleDelete = async (id) => {
        const confirmed = window.confirm(
            'Are you sure you want to delete this company?'
        )

        if (!confirmed) {
            return
        }

        try {
            const response = await fetch(`${API_URL}/${id}`, {
                method: 'DELETE'
            })

            if (!response.ok) {
                throw new Error('Failed to delete company')
            }

            setMessage('Company deleted successfully')
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
                    <p>Register and manage rental companies.</p>
                </div>

                <button
                    className="primary-button"
                    onClick={() => setShowForm((previous) => !previous)}
                >
                    + Add Company
                </button>

            </div>

            {message && (
                <div className={`company-message ${messageType}`}>
                    {message}
                </div>
            )}

            {showForm && (
                <div className="company-form-card">

                    <h3>Register New Company</h3>

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
                                <label>Registration No</label>

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
                                <label>Phone</label>

                                <input
                                    type="text"
                                    name="phone"
                                    value={form.phone}
                                    onChange={handleChange}
                                    placeholder="Enter phone number"
                                />
                            </div>

                            <div className="form-group full-width">
                                <label>Address</label>

                                <textarea
                                    name="address"
                                    value={form.address}
                                    onChange={handleChange}
                                    placeholder="Enter company address"
                                    rows="4"
                                />
                            </div>

                            <div className="form-group">
                                <label>
                                    Registered By User ID <span>*</span>
                                </label>

                                <input
                                    type="number"
                                    name="registeredByUserId"
                                    value={form.registeredByUserId}
                                    onChange={handleChange}
                                    required
                                />

                                <small>
                                    Enter an existing sys_user user ID.
                                </small>
                            </div>

                            <div className="form-group">
                                <label>Status</label>

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

                        </div>

                        <div className="form-actions">

                            <button
                                type="submit"
                                className="primary-button"
                            >
                                Save Company
                            </button>

                            <button
                                type="button"
                                className="secondary-button"
                                onClick={() => {
                                    setShowForm(false)
                                    setMessage('')
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
                    <h3>Registered Companies</h3>
                    <span>{companies.length} companies</span>
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

                                        <td>{id}</td>

                                        <td>{name}</td>

                                        <td>
                                            {registrationNo || '-'}
                                        </td>

                                        <td>{company.email}</td>

                                        <td>
                                            {company.phone || '-'}
                                        </td>

                                        <td>
                                            {company.address || '-'}
                                        </td>

                                        <td>
                                                <span className="status-badge">
                                                    {status}
                                                </span>
                                        </td>

                                        <td>
                                            <button
                                                className="delete-button"
                                                onClick={() =>
                                                    handleDelete(id)
                                                }
                                            >
                                                Delete
                                            </button>
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