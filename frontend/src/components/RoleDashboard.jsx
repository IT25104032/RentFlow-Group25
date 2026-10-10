import { useEffect, useState } from 'react'
import CompanyManagement from './CompanyManagement'
import Users from '../pages/Users'
import EquipmentCategories from '../pages/EquipmentCategories'
import EquipmentManagement from '../pages/EquipmentManagement'
import CompanyProfile from '../pages/CompanyProfile'

function RoleDashboard({ user, onLogout }) {
    const role = (user?.role || '')
        .replace('ROLE_', '')
        .toUpperCase()

    const [activePage, setActivePage] = useState('Dashboard')

    const [stats, setStats] = useState({
        companies: 0,
        users: 0,
        categories: 0,
        equipment: 0
    })

    const compulinAdmin = role === 'COMPULIN_ADMIN'
    const rentalCompanyAdmin = role === 'COMPANY_ADMIN'
    const staff = role === 'RENTAL_OFFICER'

    let menu = []

    if (compulinAdmin) {
        menu = [
            { key: 'Dashboard', label: '📊 Dashboard' },
            { key: 'Companies', label: '🏢 Companies' }
        ]
    }

    if (rentalCompanyAdmin) {
        menu = [
            { key: 'Dashboard', label: '📊 Dashboard' },
            { key: 'Company Profile', label: '🏢 Company Profile' },
            { key: 'Users', label: '👥 Users' },
            { key: 'Equipment Categories', label: '📁 Equipment Categories' },
            { key: 'Equipment', label: '🔧 Equipment' }
        ]
    }

    if (staff) {
        menu = [
            { key: 'Dashboard', label: '📊 Dashboard' },
            { key: 'Equipment Categories', label: '📁 Equipment Categories' },
            { key: 'Equipment', label: '🔧 Equipment' }
        ]
    }

    const loadStats = async () => {
        if (!user) {
            return
        }

        if (compulinAdmin) {
            try {
                const response = await fetch(
                    'http://localhost:8081/api/module1/companies',
                    {
                        method: 'GET',
                        credentials: 'include'
                    }
                )

                if (!response.ok) {
                    throw new Error(
                        `Companies request failed: ${response.status}`
                    )
                }

                const data = await response.json()

                setStats((previous) => ({
                    ...previous,
                    companies: Array.isArray(data)
                        ? data.length
                        : 0
                }))
            } catch (error) {
                console.error(
                    'Failed to load company statistics:',
                    error
                )

                setStats((previous) => ({
                    ...previous,
                    companies: 0
                }))
            }

            return
        }

        if (rentalCompanyAdmin || staff) {
            setStats((previous) => ({
                ...previous,
                companies: 1
            }))

            try {
                const response = await fetch(
                    'http://localhost:8081/api/module1/users',
                    {
                        method: 'GET',
                        credentials: 'include'
                    }
                )

                if (!response.ok) {
                    throw new Error(
                        `Users request failed: ${response.status}`
                    )
                }

                const data = await response.json()

                setStats((previous) => ({
                    ...previous,
                    users: Array.isArray(data)
                        ? data.length
                        : 0
                }))
            } catch (error) {
                console.error(
                    'Failed to load user statistics:',
                    error
                )

                setStats((previous) => ({
                    ...previous,
                    users: 0
                }))
            }

            try {
                const response = await fetch(
                    'http://localhost:8081/api/module1/equipment-categories',
                    {
                        method: 'GET',
                        credentials: 'include'
                    }
                )

                if (!response.ok) {
                    throw new Error(
                        `Categories request failed: ${response.status}`
                    )
                }

                const data = await response.json()

                setStats((previous) => ({
                    ...previous,
                    categories: Array.isArray(data)
                        ? data.length
                        : 0
                }))
            } catch (error) {
                console.error(
                    'Failed to load category statistics:',
                    error
                )

                setStats((previous) => ({
                    ...previous,
                    categories: 0
                }))
            }

            try {
                const response = await fetch(
                    'http://localhost:8081/api/module1/equipment',
                    {
                        method: 'GET',
                        credentials: 'include'
                    }
                )

                if (!response.ok) {
                    throw new Error(
                        `Equipment request failed: ${response.status}`
                    )
                }

                const data = await response.json()

                setStats((previous) => ({
                    ...previous,
                    equipment: Array.isArray(data)
                        ? data.length
                        : 0
                }))
            } catch (error) {
                console.error(
                    'Failed to load equipment statistics:',
                    error
                )

                setStats((previous) => ({
                    ...previous,
                    equipment: 0
                }))
            }
        }
    }

    useEffect(() => {
        loadStats()
    }, [user, role])

    const renderPage = () => {
        if (activePage === 'Dashboard') {
            return (
                <>
                    <section className="welcome-card">
                        <h2>Welcome to RentFlow</h2>

                        <p>
                            {compulinAdmin &&
                                'Manage rental-company accounts from the platform dashboard.'}

                            {rentalCompanyAdmin &&
                                'Manage your company, staff and inventory from one place.'}

                            {staff &&
                                'Manage authorized equipment categories and inventory.'}
                        </p>
                    </section>

                    <section className="stats-grid">

                        {compulinAdmin && (
                            <div className="stat-card">
                                <div className="stat-icon">🏢</div>

                                <div>
                                    <span>Companies</span>
                                    <strong>{stats.companies}</strong>
                                </div>
                            </div>
                        )}

                        {(rentalCompanyAdmin || staff) && (
                            <div className="stat-card">
                                <div className="stat-icon">🏢</div>

                                <div>
                                    <span>My Company</span>
                                    <strong>{stats.companies}</strong>
                                </div>
                            </div>
                        )}

                        <div className="stat-card">
                            <div className="stat-icon">👥</div>

                            <div>
                                <span>Users</span>
                                <strong>{stats.users}</strong>
                            </div>
                        </div>

                        <div className="stat-card">
                            <div className="stat-icon">📁</div>

                            <div>
                                <span>Categories</span>
                                <strong>{stats.categories}</strong>
                            </div>
                        </div>

                        <div className="stat-card">
                            <div className="stat-icon">🔧</div>

                            <div>
                                <span>Equipment</span>
                                <strong>{stats.equipment}</strong>
                            </div>
                        </div>

                    </section>
                </>
            )
        }

        if (activePage === 'Companies' && compulinAdmin) {
            return <CompanyManagement user={user} />
        }

        if (activePage === 'Company Profile' && rentalCompanyAdmin) {
            return <CompanyProfile />
        }

        if (activePage === 'Users' && rentalCompanyAdmin) {
            return (
                <Users user={user} />
            )
        }

        if (
            activePage === 'Equipment Categories' &&
            (rentalCompanyAdmin || staff)
        ) {
            return (
                <EquipmentCategories user={user} />
            )
        }

        if (
            activePage === 'Equipment' &&
            (rentalCompanyAdmin || staff)
        ) {
            return (
                <EquipmentManagement user={user} />
            )
        }

        return (
            <div className="module-placeholder">
                <h2>Access Denied</h2>

                <p>
                    You are not authorized to access this function.
                </p>
            </div>
        )
    }

    return (
        <div className="app-container">

            <aside className="sidebar">

                <div className="logo-area">
                    <h2>RentFlow</h2>
                    <span>Module 1</span>
                </div>

                <nav>
                    {menu.map((item) => (
                        <button
                            key={item.key}
                            className={`nav-item ${
                                activePage === item.key
                                    ? 'active'
                                    : ''
                            }`}
                            onClick={() => setActivePage(item.key)}
                        >
                            {item.label}
                        </button>
                    ))}

                    <button
                        className="nav-item"
                        onClick={onLogout}
                        style={{ marginTop: '20px' }}
                    >
                        🚪 Logout
                    </button>
                </nav>

            </aside>

            <main className="main-content">

                <header className="topbar">

                    <div>
                        <h1>{activePage}</h1>

                        <p>
                            Company & Inventory Management
                        </p>
                    </div>

                    <div className="user-info">

                        <div className="user-avatar">
                            {user?.fullName
                                ? user.fullName
                                    .charAt(0)
                                    .toUpperCase()
                                : 'U'}
                        </div>

                        <div>
                            <strong>
                                {user?.fullName || 'User'}
                            </strong>

                            <small>
                                {role}
                            </small>
                        </div>

                    </div>

                </header>

                <div className="page-content">
                    {renderPage()}
                </div>

            </main>

        </div>
    )
}

export default RoleDashboard