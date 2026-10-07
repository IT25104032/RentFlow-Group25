import { useEffect, useState } from 'react'
import './EquipmentCategories.css'

const API_URL = 'http://localhost:8081/api/module1/equipment-categories'

function EquipmentCategories({ user }) {
    const [categories, setCategories] = useState([])
    const [showForm, setShowForm] = useState(false)
    const [editingId, setEditingId] = useState(null)
    const [searchTerm, setSearchTerm] = useState('')
    const [message, setMessage] = useState('')
    const [messageType, setMessageType] = useState('')

    const [form, setForm] = useState({
        categoryName: '',
        catDescription: '',
        catStatus: 'ACTIVE'
    })

    const loadCategories = async () => {
        try {
            const response = await fetch(API_URL, {
                method: 'GET',
                credentials: 'include'
            })

            if (!response.ok) {
                throw new Error('Failed to load equipment categories')
            }

            const data = await response.json()

            setCategories(
                Array.isArray(data)
                    ? data
                    : []
            )
        } catch (error) {
            console.error(error)

            setMessage(
                error.message ||
                'Failed to load equipment categories'
            )

            setMessageType('error')
        }
    }

    useEffect(() => {
        if (user) {
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
            categoryName: '',
            catDescription: '',
            catStatus: 'ACTIVE'
        })

        setEditingId(null)
        setShowForm(false)
    }

    const handleAdd = () => {
        setForm({
            categoryName: '',
            catDescription: '',
            catStatus: 'ACTIVE'
        })

        setEditingId(null)
        setMessage('')
        setMessageType('')
        setShowForm(true)
    }

    const handleEdit = (category) => {
        const id =
            category.category_id ??
            category.categoryId

        const name =
            category.category_name ??
            category.categoryName

        const description =
            category.cat_description ??
            category.catDescription

        const status =
            category.cat_status ??
            category.catStatus

        setForm({
            categoryName: name || '',
            catDescription: description || '',
            catStatus: status || 'ACTIVE'
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

        if (!form.categoryName.trim()) {
            setMessage('Category name is required')
            setMessageType('error')
            return
        }

        try {
            const url = editingId
                ? `${API_URL}/${editingId}`
                : API_URL

            const method = editingId
                ? 'PUT'
                : 'POST'

            const response = await fetch(url, {
                method,
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    categoryName: form.categoryName,
                    catDescription: form.catDescription,
                    catStatus: form.catStatus
                })
            })

            const data = await response.json().catch(() => ({}))

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    'Failed to save equipment category'
                )
            }

            setMessage(
                editingId
                    ? 'Equipment category updated successfully'
                    : 'Equipment category created successfully'
            )

            setMessageType('success')

            resetForm()

            await loadCategories()

        } catch (error) {
            console.error(error)

            setMessage(
                error.message ||
                'Failed to save equipment category'
            )

            setMessageType('error')
        }
    }

    const handleDeactivate = async (category) => {
        const id =
            category.category_id ??
            category.categoryId

        const name =
            category.category_name ??
            category.categoryName

        const description =
            category.cat_description ??
            category.catDescription

        const confirmed = window.confirm(
            `Deactivate category "${name}"?`
        )

        if (!confirmed) {
            return
        }

        try {
            const response = await fetch(
                `${API_URL}/${id}`,
                {
                    method: 'PUT',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        categoryName: name,
                        catDescription: description || '',
                        catStatus: 'INACTIVE'
                    })
                }
            )

            const data = await response.json().catch(() => ({}))

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    'Failed to deactivate category'
                )
            }

            setMessage(
                'Equipment category deactivated successfully'
            )

            setMessageType('success')

            await loadCategories()

        } catch (error) {
            console.error(error)

            setMessage(
                error.message ||
                'Failed to deactivate category'
            )

            setMessageType('error')
        }
    }

    const handleActivate = async (category) => {
        const id =
            category.category_id ??
            category.categoryId

        const name =
            category.category_name ??
            category.categoryName

        const description =
            category.cat_description ??
            category.catDescription

        try {
            const response = await fetch(
                `${API_URL}/${id}`,
                {
                    method: 'PUT',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        categoryName: name,
                        catDescription: description || '',
                        catStatus: 'ACTIVE'
                    })
                }
            )

            const data = await response.json().catch(() => ({}))

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    'Failed to activate category'
                )
            }

            setMessage(
                'Equipment category activated successfully'
            )

            setMessageType('success')

            await loadCategories()

        } catch (error) {
            console.error(error)

            setMessage(
                error.message ||
                'Failed to activate category'
            )

            setMessageType('error')
        }
    }

    const filteredCategories = categories.filter((category) => {
        const name =
            category.category_name ??
            category.categoryName ??
            ''

        const description =
            category.cat_description ??
            category.catDescription ??
            ''

        const search = searchTerm.toLowerCase()

        return (
            name.toLowerCase().includes(search) ||
            description.toLowerCase().includes(search)
        )
    })

    return (
        <div className="categories-page">

            <div className="categories-header">
                <div>
                    <h2>Equipment Categories</h2>

                    <p>
                        Create and manage your company's equipment categories.
                    </p>
                </div>

                <button
                    className="add-category-button"
                    onClick={handleAdd}
                >
                    + Add Category
                </button>
            </div>

            {message && (
                <div className={`category-message ${messageType}`}>
                    {message}
                </div>
            )}

            {showForm && (
                <div className="category-form-card">

                    <div className="category-form-header">
                        <h3>
                            {editingId
                                ? 'Edit Equipment Category'
                                : 'Add Equipment Category'}
                        </h3>

                        <button
                            className="close-form-button"
                            onClick={resetForm}
                        >
                            ✕
                        </button>
                    </div>

                    <form onSubmit={handleSubmit}>

                        <div className="category-form-grid">

                            <div className="form-group">
                                <label>
                                    Category Name
                                </label>

                                <input
                                    type="text"
                                    name="categoryName"
                                    value={form.categoryName}
                                    onChange={handleChange}
                                    placeholder="Enter category name"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>
                                    Status
                                </label>

                                <select
                                    name="catStatus"
                                    value={form.catStatus}
                                    onChange={handleChange}
                                >
                                    <option value="ACTIVE">
                                        ACTIVE
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
                                    name="catDescription"
                                    value={form.catDescription}
                                    onChange={handleChange}
                                    placeholder="Enter category description"
                                    rows="4"
                                />
                            </div>

                        </div>

                        <div className="category-form-actions">

                            <button
                                type="button"
                                className="cancel-button"
                                onClick={resetForm}
                            >
                                Cancel
                            </button>

                            <button
                                type="submit"
                                className="save-button"
                            >
                                {editingId
                                    ? 'Update Category'
                                    : 'Create Category'}
                            </button>

                        </div>

                    </form>
                </div>
            )}

            <div className="category-toolbar">

                <input
                    type="text"
                    className="category-search"
                    value={searchTerm}
                    onChange={(e) =>
                        setSearchTerm(e.target.value)
                    }
                    placeholder="Search categories..."
                />

                <span className="category-count">
                    {filteredCategories.length} categor
                    {filteredCategories.length === 1 ? 'y' : 'ies'}
                </span>

            </div>

            <div className="category-table-card">

                <div className="table-wrapper">

                    <table className="category-table">

                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Category Name</th>
                            <th>Description</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                        </thead>

                        <tbody>

                        {filteredCategories.length === 0 ? (
                            <tr>
                                <td
                                    colSpan="5"
                                    className="empty-table"
                                >
                                    No equipment categories found.
                                </td>
                            </tr>
                        ) : (
                            filteredCategories.map((category) => {

                                const id =
                                    category.category_id ??
                                    category.categoryId

                                const name =
                                    category.category_name ??
                                    category.categoryName

                                const description =
                                    category.cat_description ??
                                    category.catDescription

                                const status =
                                    category.cat_status ??
                                    category.catStatus

                                const active =
                                    status?.toUpperCase() === 'ACTIVE'

                                return (
                                    <tr key={id}>

                                        <td>
                                            {id}
                                        </td>

                                        <td>
                                            <strong>
                                                {name}
                                            </strong>
                                        </td>

                                        <td>
                                            {description || '-'}
                                        </td>

                                        <td>
                                                <span
                                                    className={
                                                        active
                                                            ? 'status-badge active'
                                                            : 'status-badge inactive'
                                                    }
                                                >
                                                    {status}
                                                </span>
                                        </td>

                                        <td>

                                            <div className="table-actions">

                                                <button
                                                    className="edit-category-button"
                                                    onClick={() =>
                                                        handleEdit(category)
                                                    }
                                                >
                                                    Edit
                                                </button>

                                                {active ? (
                                                    <button
                                                        className="deactivate-category-button"
                                                        onClick={() =>
                                                            handleDeactivate(category)
                                                        }
                                                    >
                                                        Deactivate
                                                    </button>
                                                ) : (
                                                    <button
                                                        className="activate-category-button"
                                                        onClick={() =>
                                                            handleActivate(category)
                                                        }
                                                    >
                                                        Activate
                                                    </button>
                                                )}

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

export default EquipmentCategories