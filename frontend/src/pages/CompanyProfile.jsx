
import { useEffect, useState } from 'react'
import './CompanyProfile.css'

const API = 'http://localhost:8081/api/module1/company-profile'

function CompanyProfile() {
    const [profile, setProfile] = useState({
        companyId: '',
        companyName: '',
        registrationNo: '',
        email: '',
        phone: '',
        address: '',
        registrationDate: '',
        companyStatus: ''
    })

    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const [message, setMessage] = useState('')
    const [error, setError] = useState('')

    useEffect(() => {
        loadProfile()
    }, [])

    async function loadProfile() {
        try {
            setLoading(true)
            setError('')

            const response = await fetch(API, {
                credentials: 'include'
            })

            const data = await response.json().catch(() => ({}))

            if (!response.ok) {
                throw new Error(
                    data.message || 'Could not load company profile.'
                )
            }

            setProfile(data)
        } catch (err) {
            setError(err.message)
        } finally {
            setLoading(false)
        }
    }

    function handleChange(event) {
        const { name, value } = event.target

        setProfile(previous => ({
            ...previous,
            [name]: value
        }))
    }

    async function handleSubmit(event) {
        event.preventDefault()
        setMessage('')
        setError('')

        try {
            setSaving(true)

            const response = await fetch(API, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    companyName: profile.companyName,
                    phone: profile.phone,
                    address: profile.address
                })
            })

            const data = await response.json().catch(() => ({}))

            if (!response.ok) {
                throw new Error(
                    data.message || 'Could not update company profile.'
                )
            }

            setProfile(data)
            setMessage('Company profile updated successfully.')
        } catch (err) {
            setError(err.message)
        } finally {
            setSaving(false)
        }
    }

    if (loading) {
        return (
            <div className="company-profile">
                <p>Loading company profile...</p>
            </div>
        )
    }

    return (
        <div className="company-profile">
            <div className="profile-heading">
                <div>
                    <h2>Company Profile</h2>
                    <p>View and update your rental company information.</p>
                </div>

                <span className="profile-status">
                    {profile.companyStatus || 'Unknown'}
                </span>
            </div>

            {message && (
                <div className="profile-message">{message}</div>
            )}

            {error && (
                <div className="profile-error">{error}</div>
            )}

            <form onSubmit={handleSubmit} className="profile-form">
                <label>
                    Company ID
                    <input value={profile.companyId ?? ''} disabled />
                </label>

                <label>
                    Company Name *
                    <input
                        name="companyName"
                        value={profile.companyName || ''}
                        onChange={handleChange}
                        required
                        maxLength={150}
                    />
                </label>

                <label>
                    Registration Number
                    <input
                        value={profile.registrationNo || ''}
                        disabled
                    />
                </label>

                <label>
                    Company Email
                    <input
                        type="email"
                        value={profile.email || ''}
                        disabled
                    />
                </label>

                <label>
                    Phone Number
                    <input
                        name="phone"
                        value={profile.phone || ''}
                        onChange={handleChange}
                        maxLength={25}
                    />
                </label>

                <label>
                    Address
                    <textarea
                        name="address"
                        value={profile.address || ''}
                        onChange={handleChange}
                        maxLength={255}
                        rows={3}
                    />
                </label>

                <label>
                    Registration Date
                    <input
                        value={profile.registrationDate || ''}
                        disabled
                    />
                </label>

                <button type="submit" disabled={saving}>
                    {saving ? 'Saving...' : 'Save Changes'}
                </button>
            </form>
        </div>
    )
}

export default CompanyProfile
