import { useState, type SubmitEvent } from 'react'
import { useAuth } from '../../features/auth/useAuth'
import { SettingsApi } from '../../features/settings/api/settingsApi'
import { formatApiError } from '../../shared/api/ApiError'

export default function ProfileSection() {
  const { auth, completeLogin } = useAuth()
  const user = auth.status === 'authenticated' ? auth.user : null

  const [displayName, setDisplayName] = useState(user?.displayName ?? '')
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (!user) return null

  const trimmed = displayName.trim()
  const isDirty = trimmed !== user.displayName

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!user) return

    setSaving(true)
    setError(null)

    try {
      await SettingsApi.updateDisplayName(trimmed)
      completeLogin({ ...user, displayName: trimmed })
      setSaved(true)
    } catch (cause) {
      setError(formatApiError(cause))
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="settings-section">
      <h2>Profile</h2>
      <form className="settings-form" onSubmit={handleSubmit}>
        <label>
          Display name
          <input
            value={displayName}
            onChange={e => {
              setDisplayName(e.target.value)
              setSaved(false)
            }}
            required
          />
        </label>
        {error && (
          <p className="admin-error" role="alert">
            {error}
          </p>
        )}
        {saved && !isDirty && <p className="settings-success">Saved.</p>}
        <button type="submit" className="btn-primary" disabled={saving || !isDirty}>
          {saving ? 'Saving…' : 'Save'}
        </button>
      </form>
    </section>
  )
}
