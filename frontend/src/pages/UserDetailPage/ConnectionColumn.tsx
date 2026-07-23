import { useState } from 'react'
import type { AdminUser, UserConnection } from '../../features/admin/types'
import { formatApiError } from '../../shared/api/ApiError'
import UserTypeahead from './UserTypeahead'

type ConnectionColumnProps = {
  title: string
  connections: UserConnection[]
  addLabel: string
  excludeUserIds: string[]
  onAdd: (userId: string) => Promise<void>
  onRemove: (connectionId: string) => Promise<void>
}

export default function ConnectionColumn({
  title,
  connections,
  addLabel,
  excludeUserIds,
  onAdd,
  onRemove,
}: ConnectionColumnProps) {
  const [adding, setAdding] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [removingId, setRemovingId] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function handleSelect(user: AdminUser) {
    setSubmitting(true)
    setError(null)

    try {
      await onAdd(user.id)
      setAdding(false)
    } catch (cause) {
      setError(formatApiError(cause))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleRemove(connectionId: string) {
    setRemovingId(connectionId)
    setError(null)

    try {
      await onRemove(connectionId)
    } catch (cause) {
      setError(formatApiError(cause))
    } finally {
      setRemovingId(null)
    }
  }

  return (
    <div className="connection-column">
      <h3>{title}</h3>

      {connections.length === 0 ? (
        <p className="connection-column-empty">None yet.</p>
      ) : (
        <ul className="connection-list">
          {connections.map(connection => (
            <li key={connection.connectionId} className="connection-list-item">
              <div className="user-cell">
                <div className="user-cell-names">
                  <span className="user-cell-displayname">{connection.displayName}</span>
                  <span className="user-cell-username">{connection.username}</span>
                </div>
                {!connection.enabled && (
                  <span className="status-badge status-badge--disabled">Disabled</span>
                )}
              </div>
              <button
                type="button"
                className="connection-remove-btn"
                onClick={() => void handleRemove(connection.connectionId)}
                disabled={removingId === connection.connectionId}
                aria-label={`Remove ${connection.displayName}`}
              >
                {removingId === connection.connectionId ? '…' : '✕'}
              </button>
            </li>
          ))}
        </ul>
      )}

      {error && (
        <p className="admin-error" role="alert">
          {error}
        </p>
      )}

      {adding ? (
        <UserTypeahead
          excludeUserIds={excludeUserIds}
          submitting={submitting}
          onSelect={user => void handleSelect(user)}
          onCancel={() => {
            setAdding(false)
            setError(null)
          }}
        />
      ) : (
        <button type="button" className="btn-secondary connection-add-btn" onClick={() => setAdding(true)}>
          + {addLabel}
        </button>
      )}
    </div>
  )
}
