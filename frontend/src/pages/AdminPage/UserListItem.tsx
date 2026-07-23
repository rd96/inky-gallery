import { useState } from 'react'
import { Link } from 'react-router-dom'
import { AdminApi } from '../../features/admin/api/adminApi'
import type { AdminUser } from '../../features/admin/types'
import { formatApiError } from '../../shared/api/ApiError'
import { useEscapeKey } from '../../shared/hooks/useEscapeKey'
import ActivationLinkCard from './ActivationLinkCard'

type UserListItemProps = {
  user: AdminUser
  onError: (message: string) => void
}

export default function UserListItem({ user, onError }: UserListItemProps) {
  const [generatingLink, setGeneratingLink] = useState(false)
  const [newActivationToken, setNewActivationToken] = useState<string | null>(null)

  useEscapeKey(() => setNewActivationToken(null), newActivationToken !== null)

  async function generateActivationLink() {
    setGeneratingLink(true)

    try {
      const result = await AdminApi.createActivationToken(user.id)
      setNewActivationToken(result.activationToken)
    } catch (cause) {
      onError(formatApiError(cause))
    } finally {
      setGeneratingLink(false)
    }
  }

  return (
    <li className="user-list-item">
      <Link to={`/admin/users/${user.id}`} state={user} className="user-cell-link">
        <div className="user-cell">
          <div className="user-cell-names">
            <span className="user-cell-displayname">{user.displayName}</span>
            <span className="user-cell-username">{user.username}</span>
          </div>
          <div className="user-cell-badges">
            {user.role === 'ADMIN' && (
              <span className="status-badge status-badge--admin">Admin</span>
            )}
            {!user.enabled && (
              <span className="status-badge status-badge--disabled">Disabled</span>
            )}
          </div>
        </div>
      </Link>

      <div className="user-list-item-actions">
        {user.activationStatus === 'PENDING' && (
          <button
            type="button"
            className="status-badge status-badge--pending status-badge--pill"
            onClick={generateActivationLink}
            disabled={generatingLink}
          >
            {generatingLink ? 'Generating…' : 'Pending · Generate new link'}
          </button>
        )}
      </div>

      {newActivationToken && (
        <div
          className="admin-modal-backdrop"
          onClick={() => setNewActivationToken(null)}
        >
          <ActivationLinkCard
            username={user.username}
            activationToken={newActivationToken}
            onDone={() => setNewActivationToken(null)}
          />
        </div>
      )}
    </li>
  )
}
