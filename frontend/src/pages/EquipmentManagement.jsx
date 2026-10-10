import { useEffect, useState } from 'react'
import './EquipmentManagement.css'

const EQUIPMENT_API =
    'http://localhost:8081/api/module1/equipment'

const CATEGORY_API =
    'http://localhost:8081/api/module1/equipment-categories'

function EquipmentManagement({ user }) {
    const [equipment, setEquipment] = useState([])
    const [categories, setCategories] = useState([])
    const [showForm, setShowForm] = useState(false)
    const [editingId, setEditingId] = useState(null)
    const [searchTerm, setSearchTerm] = useState('')
    const [message, setMessage] = useState('')
    const [messageType, setMessageType] = useState('')

    const [form, setForm] = useState({
        itemName: '',
        itemCode: '',
        categoryId: '',
        equDescription: '',
        rentalRate: '',
        ratePeriod: 'DAY',
        securityDepositPerUnit: '',
        totalQuantity: '',
        availableQuantity: '',
        equStatus: 'AVAILABLE'
    })

    const loadEquipment = async () => {
        try {
            const response = await fetch(
                EQUIPMENT_API,
                {
                    method: 'GET',
                    credentials: 'include'
                }
            )

            if (!response.ok) {
                throw new Error(
                    'Failed to load equipment'
                )
            }

            const data = await response.json()

            setEquipment(
                Array.isArray(data)
                    ? data
                    : []
            )

        } catch (error) {
            console.error(error)

            setMessage(error.message)
            setMessageType('error')
        }
    }

    const loadCategories = async () => {
        try {
            const response = await fetch(
                CATEGORY_API,
                {
                    method: 'GET',
                    credentials: 'include'
                }
            )

            if (!response.ok) {
                throw new Error(
                    'Failed to load categories'
                )
            }

            const data = await response.json()

            setCategories(
                Array.isArray(data)
                    ? data
                    : []
            )

        } catch (error) {
            console.error(error)

            setMessage(error.message)
            setMessageType('error')
        }
    }

    useEffect(() => {
        if (user) {
            loadEquipment()
            loadCategories()
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
            itemName: '',
            itemCode: '',
            categoryId: '',
            equDescription: '',
            rentalRate: '',
            ratePeriod: 'DAY',
            securityDepositPerUnit: '',
            totalQuantity: '',
            availableQuantity: '',
            equStatus: 'AVAILABLE'
        })

        setEditingId(null)
        setShowForm(false)
    }

    const handleAdd = () => {
        resetForm()

        setMessage('')
        setMessageType('')
        setShowForm(true)
    }

    const handleEdit = (item) => {
        setForm({
            itemName: item.itemName || '',
            itemCode: item.itemCode || '',
            categoryId: item.categoryId || '',
            equDescription: item.equDescription || '',
            rentalRate: item.rentalRate || '',
            ratePeriod: item.ratePeriod || 'DAY',
            securityDepositPerUnit:
                item.securityDepositPerUnit || '',
            totalQuantity:
                item.totalQuantity || '',
            availableQuantity:
                item.availableQuantity ?? '',
            equStatus:
                item.equStatus || 'AVAILABLE'
        })

        setEditingId(item.equipmentId)
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

        if (!form.itemName.trim()) {
            setMessage(
                'Item name is required'
            )
            setMessageType('error')
            return
        }

        if (!form.categoryId) {
            setMessage(
                'Please select a category'
            )
            setMessageType('error')
            return
        }

        if (
            form.rentalRate === '' ||
            Number(form.rentalRate) < 0
        ) {
            setMessage(
                'Rental rate must be zero or greater'
            )
            setMessageType('error')
            return
        }

        if (
            form.securityDepositPerUnit === '' ||
            Number(form.securityDepositPerUnit) < 0
        ) {
            setMessage(
                'Security deposit must be zero or greater'
            )
            setMessageType('error')
            return
        }

        if (
            form.totalQuantity === '' ||
            Number(form.totalQuantity) <= 0
        ) {
            setMessage(
                'Total quantity must be greater than zero'
            )
            setMessageType('error')
            return
        }

        try {
            const url = editingId
                ? `${EQUIPMENT_API}/${editingId}`
                : EQUIPMENT_API

            const method = editingId
                ? 'PUT'
                : 'POST'

            const body = {
                itemName: form.itemName,
                itemCode: form.itemCode,
                categoryId: Number(form.categoryId),
                equDescription: form.equDescription,
                rentalRate: Number(form.rentalRate),
                ratePeriod: form.ratePeriod,
                securityDepositPerUnit:
                    Number(
                        form.securityDepositPerUnit
                    ),
                totalQuantity:
                    Number(form.totalQuantity),
                availableQuantity:
                    editingId
                        ? Number(form.availableQuantity)
                        : Number(form.totalQuantity),
                equStatus: form.equStatus
            }

            const response = await fetch(
                url,
                {
                    method,
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(body)
                }
            )

            const data = await response.json()
                .catch(() => ({}))

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    'Failed to save equipment'
                )
            }

            setMessage(
                editingId
                    ? 'Equipment updated successfully'
                    : 'Equipment registered successfully'
            )

            setMessageType('success')

            resetForm()

            await loadEquipment()

        } catch (error) {
            console.error(error)

            setMessage(
                error.message ||
                'Failed to save equipment'
            )

            setMessageType('error')
        }
    }

    const changeStatus = async (
        item,
        status
    ) => {

        try {
            const response = await fetch(
                `${EQUIPMENT_API}/${item.equipmentId}/status?status=${status}`,
                {
                    method: 'PUT',
                    credentials: 'include'
                }
            )

            const data = await response.json()
                .catch(() => ({}))

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    'Failed to update equipment status'
                )
            }

            setMessage(
                `Equipment status changed to ${status}`
            )

            setMessageType('success')

            await loadEquipment()

        } catch (error) {
            console.error(error)

            setMessage(
                error.message ||
                'Failed to update equipment status'
            )

            setMessageType('error')
        }
    }

    const handleDeactivate = async (
        item
    ) => {

        const confirmed = window.confirm(
            `Deactivate "${item.itemName}"?`
        )

        if (!confirmed) {
            return
        }

        try {
            const response = await fetch(
                `${EQUIPMENT_API}/${item.equipmentId}/deactivate`,
                {
                    method: 'PUT',
                    credentials: 'include'
                }
            )

            const data = await response.json()
                .catch(() => ({}))

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    'Failed to deactivate equipment'
                )
            }

            setMessage(
                'Equipment deactivated successfully'
            )

            setMessageType('success')

            await loadEquipment()

        } catch (error) {
            console.error(error)

            setMessage(
                error.message ||
                'Failed to deactivate equipment'
            )

            setMessageType('error')
        }
    }

    const filteredEquipment =
        equipment.filter((item) => {

            const search =
                searchTerm.toLowerCase()

            return (
                (item.itemName || '')
                    .toLowerCase()
                    .includes(search) ||
                (item.itemCode || '')
                    .toLowerCase()
                    .includes(search) ||
                (item.categoryName || '')
                    .toLowerCase()
                    .includes(search)
            )
        })

    return (
        <div className="equipment-page">

            <div className="equipment-header">

                <div>
                    <h2>
                        Equipment Management
                    </h2>

                    <p>
                        Register and manage your company's equipment inventory.
                    </p>
                </div>

                <button
                    className="add-equipment-button"
                    onClick={handleAdd}
                >
                    + Add Equipment
                </button>

            </div>

            {message && (
                <div
                    className={`equipment-message ${messageType}`}
                >
                    {message}
                </div>
            )}

            {showForm && (
                <div className="equipment-form-card">

                    <div className="equipment-form-header">

                        <h3>
                            {editingId
                                ? 'Edit Equipment'
                                : 'Register Equipment'}
                        </h3>

                        <button
                            className="close-equipment-form"
                            onClick={resetForm}
                        >
                            ✕
                        </button>

                    </div>

                    <form onSubmit={handleSubmit}>

                        <div className="equipment-form-grid">

                            <div className="form-group">
                                <label>
                                    Item Name
                                </label>

                                <input
                                    type="text"
                                    name="itemName"
                                    value={form.itemName}
                                    onChange={handleChange}
                                    placeholder="Enter item name"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>
                                    Item Code
                                </label>

                                <input
                                    type="text"
                                    name="itemCode"
                                    value={form.itemCode}
                                    onChange={handleChange}
                                    placeholder="Enter item code"
                                />
                            </div>

                            <div className="form-group">
                                <label>
                                    Category
                                </label>

                                <select
                                    name="categoryId"
                                    value={form.categoryId}
                                    onChange={handleChange}
                                    required
                                >
                                    <option value="">
                                        Select category
                                    </option>

                                    {categories.map(
                                        (category) => (
                                            <option
                                                key={
                                                    category.category_id ??
                                                    category.categoryId ??
                                                    category.categoryId
                                                }
                                                value={
                                                    category.category_id ??
                                                    category.categoryId
                                                }
                                            >
                                                {
                                                    category.category_name ??
                                                    category.categoryName
                                                }
                                            </option>
                                        )
                                    )}
                                </select>
                            </div>

                            <div className="form-group">
                                <label>
                                    Rate Period
                                </label>

                                <select
                                    name="ratePeriod"
                                    value={form.ratePeriod}
                                    onChange={handleChange}
                                >
                                    <option value="HOUR">
                                        HOUR
                                    </option>

                                    <option value="DAY">
                                        DAY
                                    </option>

                                    <option value="WEEK">
                                        WEEK
                                    </option>

                                    <option value="MONTH">
                                        MONTH
                                    </option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label>
                                    Rental Rate
                                </label>

                                <input
                                    type="number"
                                    name="rentalRate"
                                    value={form.rentalRate}
                                    onChange={handleChange}
                                    min="0"
                                    step="0.01"
                                    placeholder="0.00"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>
                                    Security Deposit / Unit
                                </label>

                                <input
                                    type="number"
                                    name="securityDepositPerUnit"
                                    value={
                                        form.securityDepositPerUnit
                                    }
                                    onChange={handleChange}
                                    min="0"
                                    step="0.01"
                                    placeholder="0.00"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>
                                    Total Quantity
                                </label>

                                <input
                                    type="number"
                                    name="totalQuantity"
                                    value={form.totalQuantity}
                                    onChange={handleChange}
                                    min="1"
                                    step="1"
                                    required
                                />
                            </div>

                            {editingId && (
                                <div className="form-group">
                                    <label>
                                        Available Quantity
                                    </label>

                                    <input
                                        type="number"
                                        name="availableQuantity"
                                        value={
                                            form.availableQuantity
                                        }
                                        onChange={handleChange}
                                        min="0"
                                        step="1"
                                        required
                                    />
                                </div>
                            )}

                            <div className="form-group">
                                <label>
                                    Status
                                </label>

                                <select
                                    name="equStatus"
                                    value={form.equStatus}
                                    onChange={handleChange}
                                >
                                    <option value="AVAILABLE">
                                        AVAILABLE
                                    </option>

                                    <option value="RENTED">
                                        RENTED
                                    </option>

                                    <option value="DAMAGED">
                                        DAMAGED
                                    </option>

                                    <option value="LOST">
                                        LOST
                                    </option>

                                    <option value="MAINTENANCE">
                                        MAINTENANCE
                                    </option>

                                    <option value="INACTIVE">
                                        INACTIVE
                                    </option>
                                </select>
                            </div>

                            <div className="form-group full-width">
                                <label>
                                    Description
                                </label>

                                <textarea
                                    name="equDescription"
                                    value={
                                        form.equDescription
                                    }
                                    onChange={handleChange}
                                    rows="4"
                                    placeholder="Enter equipment description"
                                />
                            </div>

                        </div>

                        <div className="equipment-form-actions">

                            <button
                                type="button"
                                className="cancel-equipment-button"
                                onClick={resetForm}
                            >
                                Cancel
                            </button>

                            <button
                                type="submit"
                                className="save-equipment-button"
                            >
                                {editingId
                                    ? 'Update Equipment'
                                    : 'Register Equipment'}
                            </button>

                        </div>

                    </form>

                </div>
            )}

            <div className="equipment-toolbar">

                <input
                    type="text"
                    className="equipment-search"
                    value={searchTerm}
                    onChange={(e) =>
                        setSearchTerm(
                            e.target.value
                        )
                    }
                    placeholder="Search equipment..."
                />

                <span className="equipment-count">
                    {filteredEquipment.length}{' '}
                    {filteredEquipment.length === 1
                        ? 'item'
                        : 'items'}
                </span>

            </div>

            <div className="equipment-table-card">

                <div className="equipment-table-wrapper">

                    <table className="equipment-table">

                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Item</th>
                            <th>Code</th>
                            <th>Category</th>
                            <th>Rate</th>
                            <th>Deposit</th>
                            <th>Total</th>
                            <th>Available</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                        </thead>

                        <tbody>

                        {filteredEquipment.length === 0 ? (
                            <tr>
                                <td
                                    colSpan="10"
                                    className="empty-equipment"
                                >
                                    No equipment found.
                                </td>
                            </tr>
                        ) : (
                            filteredEquipment.map(
                                (item) => {

                                    const status =
                                        (
                                            item.equStatus ||
                                            ''
                                        ).toUpperCase()

                                    return (
                                        <tr
                                            key={
                                                item.equipmentId
                                            }
                                        >

                                            <td>
                                                {
                                                    item.equipmentId
                                                }
                                            </td>

                                            <td>
                                                <strong>
                                                    {
                                                        item.itemName
                                                    }
                                                </strong>
                                            </td>

                                            <td>
                                                {
                                                    item.itemCode ||
                                                    '-'
                                                }
                                            </td>

                                            <td>
                                                {
                                                    item.categoryName ||
                                                    '-'
                                                }
                                            </td>

                                            <td>
                                                {item.rentalRate}{' '}
                                                /{' '}
                                                {
                                                    item.ratePeriod
                                                }
                                            </td>

                                            <td>
                                                {
                                                    item.securityDepositPerUnit
                                                }
                                            </td>

                                            <td>
                                                {
                                                    item.totalQuantity
                                                }
                                            </td>

                                            <td>
                                                {
                                                    item.availableQuantity
                                                }
                                            </td>

                                            <td>
                                                    <span
                                                        className={`equipment-status ${status.toLowerCase()}`}
                                                    >
                                                        {status}
                                                    </span>
                                            </td>

                                            <td>

                                                <div className="equipment-actions">

                                                    <button
                                                        className="edit-equipment-button"
                                                        onClick={() =>
                                                            handleEdit(
                                                                item
                                                            )
                                                        }
                                                    >
                                                        Edit
                                                    </button>

                                                    {status !==
                                                        'INACTIVE' && (
                                                            <button
                                                                className="deactivate-equipment-button"
                                                                onClick={() =>
                                                                    handleDeactivate(
                                                                        item
                                                                    )
                                                                }
                                                            >
                                                                Deactivate
                                                            </button>
                                                        )}

                                                    {status !==
                                                        'AVAILABLE' &&
                                                        status !==
                                                        'INACTIVE' && (
                                                            <button
                                                                className="available-equipment-button"
                                                                onClick={() =>
                                                                    changeStatus(
                                                                        item,
                                                                        'AVAILABLE'
                                                                    )
                                                                }
                                                            >
                                                                Available
                                                            </button>
                                                        )}

                                                </div>

                                            </td>

                                        </tr>
                                    )
                                }
                            )
                        )}

                        </tbody>

                    </table>

                </div>

            </div>

        </div>
    )
}

export default EquipmentManagement