import { useEffect, useState } from 'react'
import CompanyManagement from './CompanyManagement'
import Users from '../pages/Users'

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

    useEffect(() => {
        loadStats()
    }, [user])

    const loadStats = async () => {
        try {
            if (compulinAdmin) {
                const companies = await fetch(
                    'http://localhost:8081/api/module1/companies'
                )

                const users = await fetch(
                    'http://localhost:8081/api/module1/users'
                )

                const categories = await fetch(
                    'http://localhost:8081/api/module1/equipment-categories'
                )

                const equipment = await fetch(
                    'http://localhost:8081/api/module1/equipment'
                )

                const companyData = await companies.json()
                const userData = await users.json()
                const categoryData = await categories.json()
                const equipmentData = await equipment.json()

                setStats({
                    companies: Array.isArray(companyData)
                        ? companyData.length
                        : 0,

                    users: Array.isArray(userData)
                        ? userData.length
                        : 0,

                    categories: Array.isArray(categoryData)
                        ? categoryData.length
                        : 0,

                    equipment: Array.isArray(equipmentData)
                        ? equipmentData.length
                        : 0
                })

                return
            }

            if (user?.companyId) {
                const users = await fetch(
                    `http://localhost:8081/api/module1/users/company/${user.companyId}`
                )

                const categories = await fetch(
                    `http://localhost:8081/api/module1/equipment-categories/company/${user.companyId}`
                )

                const equipment = await fetch(
                    `http://localhost:8081/api/module1/equipment/company/${user.companyId}`
                )

                const userData = await users.json()
                const categoryData = await categories.json()
                const equipmentData = await equipment.json()

                setStats({
                    companies: 1,

                    users: Array.isArray(userData)
                        ? userData.length
                        : 0,

                    categories: Array.isArray(categoryData)
                        ? categoryData.length
                        : 0,

                    equipment: Array.isArray(equipmentData)
                        ? equipmentData.length
                        : 0
                })
            }

        } catch (error) {
            console.log('Failed to load dashboard statistics')
        }
    }

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
            return (
                <div className="module-placeholder">
                    <h2>Company Profile</h2>
                    <p>
                        View and update your rental company's profile.
                    </p>

                    <p>
                        Company ID: {user.companyId}
                    </p>
                </div>
            )
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
                <div className="module-placeholder">
                    <h2>Equipment Categories</h2>
                    <p>
                        Create and manage equipment categories.
                    </p>
                </div>
            )
        }

        if (
            activePage === 'Equipment' &&
            (rentalCompanyAdmin || staff)
        ) {
            return (
                <div className="module-placeholder">
                    <h2>Equipment Management</h2>
                    <p>
                        Register, search, update and manage equipment.
                    </p>
                </div>
            )
        }

        return (
            <div className="module-placeholder">
                <h2>Access Denied</h2>
                <p>You are not authorized to access this function.</p>
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
                                activePage === item.key ? 'active' : ''
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
                        <p>Company & Inventory Management</p>
                    </div>

                    <div className="user-info">

                        <div className="user-avatar">
                            {user?.fullName
                                ? user.fullName.charAt(0).toUpperCase()
                                : 'U'}
                        </div>

                        <div>
                            <strong>{user?.fullName}</strong>
                            <small>{role}</small>
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