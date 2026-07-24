import { useRef, useState, type SubmitEvent } from 'react'
import { AdminApi } from '../../features/admin/api/adminApi'
import type { Role } from '../../features/auth/types'
import { formatApiError } from '../../shared/api/ApiError'
import FlipCard from '../../shared/components/FlipCard'
import { useEscapeKey } from '../../shared/hooks/useEscapeKey'
import ActivationLinkCard from './ActivationLinkCard'

type CreateUserFormProps = {
  onCreated: () => void
}

type CreatedUser = {
  userId: string
  username: string
  activationToken: string
  expiresAt: string
}

export default function CreateUserForm({ onCreated }: CreateUserFormProps) {
  const [open, setOpen] = useState(false)
  const [username, setUsername] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [role, setRole] = useState<Role>('USER')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [created, setCreated] = useState<CreatedUser | null>(null)
  const cancelledRef = useRef(false)

  function close() {
    cancelledRef.current = true
    setOpen(false)
    setCreated(null)
    setUsername('')
    setDisplayName('')
    setRole('USER')
    setError(null)
  }

  function handleDismiss() {
    const isDirty = username.trim() !== '' || displayName.trim() !== ''
    if (created === null && isDirty) return
    close()
  }

  useEscapeKey(handleDismiss, open)

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    cancelledRef.current = false
    setSubmitting(true)
    setError(null)

    try {
      const { userId } = await AdminApi.createUser({ username, displayName, role })
      const { activationToken, expiresAt } = await AdminApi.createActivationToken(userId)
      onCreated()

      if (!cancelledRef.current) {
        setCreated({ userId, username, activationToken, expiresAt })
      }
    } catch (cause) {
      if (!cancelledRef.current) {
        setError(formatApiError(cause))
      }
    } finally {
      if (!cancelledRef.current) {
        setSubmitting(false)
      }
    }
  }

  if (!open) {
    return (
      <button
        type="button"
        className="admin-new-user-toggle"
        onClick={() => setOpen(true)}
      >
        New user
      </button>
    )
  }

  return (
    <div className="admin-modal-backdrop" onClick={handleDismiss}>
      <FlipCard
        className="create-user-flip-card"
        flipped={created !== null}
        onClick={e => e.stopPropagation()}
        front={
          <form
            className="modal-card"
            onSubmit={handleSubmit}
            role="dialog"
            aria-modal="true"
            aria-label="Create user"
          >
            <h2>New user</h2>
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
              <label>
                Role
                <select value={role} onChange={e => setRole(e.target.value as Role)}>
                  <option value="USER">User</option>
                  <option value="ADMIN">Admin</option>
                </select>
              </label>
            </div>
            {error && (
              <p className="admin-error" role="alert">
                {error}
              </p>
            )}
            <div className="modal-actions">
              <button type="submit" className="btn-primary" disabled={submitting}>
                {submitting ? 'Creating…' : 'Create user'}
              </button>
              <button
                type="button"
                className="btn-secondary"
                onClick={close}
                disabled={submitting}
              >
                Cancel
              </button>
            </div>
          </form>
        }
        back={
          created && (
            <ActivationLinkCard
              username={created.username}
              activationToken={created.activationToken}
              expiresAt={created.expiresAt}
              onDone={close}
            />
          )
        }
      />
    </div>
  )
}
