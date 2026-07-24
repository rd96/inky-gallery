import { useState, type SubmitEvent } from 'react'
import { useAuth } from '../../features/auth/useAuth'
import { SettingsApi } from '../../features/settings/api/settingsApi'
import { formatApiError } from '../../shared/api/ApiError'
import { useEscapeKey } from '../../shared/hooks/useEscapeKey'

export default function ProfileSection() {
  const { auth, completeLogin } = useAuth()
  const user = auth.status === 'authenticated' ? auth.user : null

  const [editing, setEditing] = useState(false)
  const [displayName, setDisplayName] = useState(user?.displayName ?? '')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function cancelEditing() {
    if (!user) return
    setDisplayName(user.displayName)
    setError(null)
    setEditing(false)
  }

  useEscapeKey(cancelEditing, editing)

  if (!user) return null

  function startEditing() {
    if (!user) return
    setDisplayName(user.displayName)
    setError(null)
    setEditing(true)
  }

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!user) return

    const trimmed = displayName.trim()
    setSaving(true)
    setError(null)

    try {
      await SettingsApi.updateDisplayName(trimmed)
      completeLogin({ ...user, displayName: trimmed })
      setEditing(false)
    } catch (cause) {
      setError(formatApiError(cause))
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="settings-section">
      <h2>Profile</h2>
      {editing ? (
        <form className="settings-form" onSubmit={handleSubmit}>
          <label>
            Display name
            <input
              value={displayName}
              onChange={e => setDisplayName(e.target.value)}
              autoFocus
              required
            />
          </label>
          {error && (
            <p className="admin-error" role="alert">
              {error}
            </p>
          )}
          <div className="modal-actions">
            <button
              type="submit"
              className="btn-primary"
              disabled={saving || displayName.trim() === ''}
            >
              {saving ? 'Saving…' : 'Save'}
            </button>
            <button
              type="button"
              className="btn-secondary"
              onClick={cancelEditing}
              disabled={saving}
            >
              Cancel
            </button>
          </div>
        </form>
      ) : (
        <button
          type="button"
          className="profile-display"
          onClick={startEditing}
          aria-label="Edit display name"
        >
          <span className="profile-displayname">{user.displayName}</span>
          <span className="profile-edit-icon" aria-hidden="true">
            ✎
          </span>
        </button>
      )}
    </section>
  )
}
