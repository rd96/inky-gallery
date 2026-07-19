import { useState, type SubmitEvent } from 'react'
import { AdminApi } from '../../features/admin/api/adminApi'
import type { AdminUser } from '../../features/admin/types'
import { useAuth } from '../../features/auth/useAuth'
import { useIsSelf } from '../../features/auth/useIsSelf'
import { formatApiError } from '../../shared/api/ApiError'
import { useEscapeKey } from '../../shared/hooks/useEscapeKey'

type EditUserModalProps = {
  user: AdminUser
  onClose: () => void
  onSaved: () => void
}

export default function EditUserModal({ user, onClose, onSaved }: EditUserModalProps) {
  const { auth, completeLogin } = useAuth()
  const isSelf = useIsSelf(user.username)
  const [username, setUsername] = useState(user.username)
  const [displayName, setDisplayName] = useState(user.displayName)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const isDirty = username !== user.username || displayName !== user.displayName

  function handleBackdropClick() {
    if (isDirty) return
    onClose()
  }

  useEscapeKey(handleBackdropClick)

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)

    try {
      await AdminApi.updateUser(user.id, { username, displayName })

      if (isSelf && auth.status === 'authenticated') {
        completeLogin({ ...auth.user, username, displayName })
      }

      onSaved()
    } catch (cause) {
      setError(formatApiError(cause))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="admin-modal-backdrop" onClick={handleBackdropClick}>
      <form
        className="modal-card"
        onSubmit={handleSubmit}
        onClick={e => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-label="Edit user"
      >
        <h2>Edit user</h2>
        <div className="modal-fields">
          <label>
            Username
            <input
              value={username}
              onChange={e => setUsername(e.target.value)}
              autoFocus
              required
            />
          </label>
          <label>
            Display name
            <input
              value={displayName}
              onChange={e => setDisplayName(e.target.value)}
              required
            />
          </label>
        </div>
        {error && (
          <p className="admin-error" role="alert">
            {error}
          </p>
        )}
        <div className="modal-actions">
          <button type="submit" className="btn-primary" disabled={submitting}>
            {submitting ? 'Saving…' : 'Save'}
          </button>
          <button
            type="button"
            className="btn-secondary"
            onClick={onClose}
            disabled={submitting}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}
