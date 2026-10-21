import { useEffect, useState } from 'react'
import './Dashboard.css'

const API = '/api/module1'

function Dashboard({ user }) {
    const role = (user?.role || '').replace(/^ROLE_/, '').toUpperCase()
    const compulinAdmin = role === 'COMPULIN_ADMIN'
    const rentalCompanyAdmin = role === 'COMPANY_ADMIN'
    const staff = role === 'RENTAL_OFFICER'

    const [stats, setStats] = useState({ companies: null, users: null, categories: null, equipment: null })
    const [error, setError] = useState('')

    useEffect(() => {
        if (!user) return
        let cancelled = false

        const count = async (endpoint) => {
            const response = await fetch(`${API}/${endpoint}`, { credentials: 'include' })
            if (!response.ok) throw new Error(`${endpoint} request failed (${response.status})`)
            const data = await response.json()
            if (!Array.isArray(data)) throw new Error(`Invalid ${endpoint} response`)
            return data.length
        }

        async function loadStats() {
            setError('')
            setStats({ companies: null, users: null, categories: null, equipment: null })
            const requests = compulinAdmin
                ? [['companies', 'companies']]
                : [['users', 'users'], ['categories', 'equipment-categories'], ['equipment', 'equipment']]
            const results = await Promise.allSettled(requests.map(async ([key, endpoint]) => [key, await count(endpoint)]))
            if (cancelled) return
            const next = { companies: compulinAdmin ? null : 1, users: null, categories: null, equipment: null }
            const failures = []
            results.forEach((result, index) => {
                if (result.status === 'fulfilled') next[result.value[0]] = result.value[1]
                else failures.push(requests[index][1])
            })
            setStats(next)
            if (failures.length) setError(`Could not load: ${failures.join(', ')}.`)
        }

        loadStats()
        return () => { cancelled = true }
    }, [user?.userId, role, compulinAdmin])

    const showCount = (value) => value === null ? '—' : value

    return (
        <div className="module1-dashboard-original">
            <section className="welcome-card">
                <h2>Welcome to RentFlow</h2>
                <p>
                    {compulinAdmin && 'Manage rental-company accounts from the platform dashboard.'}
                    {rentalCompanyAdmin && 'Manage your company, staff and inventory from one place.'}
                    {staff && 'Search equipment and categories, and check availability before a rental.'}
                </p>
            </section>

            {error && <p className="dashboard-stats-error" role="alert">{error}</p>}

            <section className="stats-grid">
                {compulinAdmin && (
                    <div className="stat-card">
                        <div className="stat-icon">🏢</div>
                        <div><span>Companies</span><strong>{showCount(stats.companies)}</strong></div>
                    </div>
                )}
                {(rentalCompanyAdmin || staff) && (
                    <>
                        <div className="stat-card">
                            <div className="stat-icon">🏢</div>
                            <div><span>My Company</span><strong>{showCount(stats.companies)}</strong></div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-icon">👥</div>
                            <div><span>Users</span><strong>{showCount(stats.users)}</strong></div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-icon">📁</div>
                            <div><span>Categories</span><strong>{showCount(stats.categories)}</strong></div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-icon">🔧</div>
                            <div><span>Equipment</span><strong>{showCount(stats.equipment)}</strong></div>
                        </div>
                    </>
                )}
            </section>
        </div>
    )
}

export default Dashboard
