import { useState } from 'react'
import { AdminApi } from '../../features/admin/api/adminApi'
import type { AdminUser } from '../../features/admin/types'
import { ApiError } from '../../shared/api/ApiError'
import ActivationLinkCard from './ActivationLinkCard'
import UserActionsMenu from './UserActionsMenu'

type UserListItemProps = {
  user: AdminUser
  isSelf: boolean
  onChanged: () => void
  onError: (message: string) => void
}

export default function UserListItem({
  user,
  isSelf,
  onChanged,
  onError,
}: UserListItemProps) {
  const [generatingLink, setGeneratingLink] = useState(false)
  const [newActivationToken, setNewActivationToken] = useState<string | null>(null)

  async function generateActivationLink() {
    setGeneratingLink(true)

    try {
      const result = await AdminApi.createActivationToken(user.id)
      setNewActivationToken(result.activationToken)
    } catch (cause) {
      onError(
        cause instanceof ApiError
          ? cause.message
          : 'Something went wrong. Please try again.',
      )
    } finally {
      setGeneratingLink(false)
    }
  }

  return (
    <li className="user-list-item">
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
        <UserActionsMenu
          user={user}
          isSelf={isSelf}
          onChanged={onChanged}
          onError={onError}
        />
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
