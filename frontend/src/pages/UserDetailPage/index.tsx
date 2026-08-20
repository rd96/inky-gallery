import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { AdminApi } from '../../features/admin/api/adminApi'
import type { AdminUser } from '../../features/admin/types'
import type { Role } from '../../features/auth/types'
import { useAuth } from '../../features/auth/useAuth'
import { ApiError, formatApiError } from '../../shared/api/ApiError'
import FullPageMessage from '../../shared/components/FullPageMessage'
import ActivationLinkCard from '../AdminPage/ActivationLinkCard'
import EditUserModal from '../AdminPage/EditUserModal'
import PasswordResetLinkCard from '../AdminPage/PasswordResetLinkCard'
import '../AdminPage/AdminPage.css'
import ConnectionsSection from './ConnectionsSection'
import './UserDetailPage.css'

export default function UserDetailPage() {
  const { userId } = useParams<{ userId: string }>()
  const navigate = useNavigate()
  const location = useLocation()
  const { auth } = useAuth()

  const [user, setUser] = useState<AdminUser | null>(
    (location.state as AdminUser | null) ?? null,
  )
  const [loading, setLoading] = useState(user === null)
  const [notFound, setNotFound] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [togglingEnabled, setTogglingEnabled] = useState(false)
  const [togglingRole, setTogglingRole] = useState(false)
  const [generatingLink, setGeneratingLink] = useState(false)
  const [newActivation, setNewActivation] = useState<{
    activationToken: string
    expiresAt: string
  } | null>(null)
  const [generatingResetLink, setGeneratingResetLink] = useState(false)
  const [newPasswordReset, setNewPasswordReset] = useState<{
    passwordResetToken: string
    expiresAt: string
  } | null>(null)
  const [editing, setEditing] = useState(false)

  useEffect(() => {
    if (!userId) return

    AdminApi.getUser(userId)
      .then(setUser)
      .catch(cause => {
        if (cause instanceof ApiError && cause.status === 404) {
          setNotFound(true)
        } else {
          setError(formatApiError(cause))
        }
      })
      .finally(() => setLoading(false))
  }, [userId])

  const isSelf = auth.status === 'authenticated' && user !== null && auth.user.userId === user.id

  async function toggleEnabled() {
    if (!user) return
    setTogglingEnabled(true)

    try {
      await AdminApi.updateUser(user.id, { enabled: !user.enabled })
      setUser({ ...user, enabled: !user.enabled })
    } catch (cause) {
      setError(formatApiError(cause))
    } finally {
      setTogglingEnabled(false)
    }
  }

  async function toggleRole() {
    if (!user) return
    const nextRole: Role = user.role === 'ADMIN' ? 'USER' : 'ADMIN'
    setTogglingRole(true)

    try {
      await AdminApi.updateUser(user.id, { role: nextRole })
      setUser({ ...user, role: nextRole })
    } catch (cause) {
      setError(formatApiError(cause))
    } finally {
      setTogglingRole(false)
    }
  }

  async function generateActivationLink() {
    if (!user) return
    setGeneratingLink(true)

    try {
      const result = await AdminApi.createActivationToken(user.id)
      setNewActivation(result)
    } catch (cause) {
      setError(formatApiError(cause))
    } finally {
      setGeneratingLink(false)
    }
  }

  async function generatePasswordResetLink() {
    if (!user) return
    setGeneratingResetLink(true)

    try {
      const result = await AdminApi.createPasswordResetToken(user.id)
      setNewPasswordReset(result)
    } catch (cause) {
      setError(formatApiError(cause))
    } finally {
      setGeneratingResetLink(false)
    }
  }

  if (notFound) {
    return (
      <FullPageMessage
        heading="User not found"
        message="This user may have been removed."
        actionLabel="Back to users"
        onAction={() => navigate('/admin')}
      />
    )
  }

  if (loading || !user) {
    return (
      <div className="admin-page">
        <p>Loading…</p>
      </div>
    )
  }

  return (
    <div className="admin-page">
      <Link to="/admin" className="user-detail-back">
        ← Back to users
      </Link>

      <div className="user-detail-header">
        <div className="user-cell-names">
          <span className="user-detail-displayname">{user.displayName}</span>
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

      {error && (
        <p className="admin-error" role="alert">
          {error}
        </p>
      )}

      <div className="user-detail-actions">
        <button type="button" className="btn-secondary" onClick={() => setEditing(true)}>
          Edit
        </button>
        <button
          type="button"
          className="btn-secondary"
          onClick={() => void toggleRole()}
          disabled={isSelf || togglingRole}
          title={isSelf ? "You can't change your own role" : undefined}
        >
          {togglingRole ? 'Saving…' : user.role === 'ADMIN' ? 'Remove admin' : 'Make admin'}
        </button>
        <button
          type="button"
          className={user.enabled ? 'btn-secondary user-actions-danger' : 'btn-secondary'}
          onClick={() => void toggleEnabled()}
          disabled={isSelf || togglingEnabled}
          title={isSelf ? "You can't disable your own account" : undefined}
        >
          {togglingEnabled ? 'Saving…' : user.enabled ? 'Disable' : 'Enable'}
        </button>
        {user.activationStatus === 'PENDING' && (
          <button
            type="button"
            className="status-badge status-badge--pending status-badge--pill"
            onClick={() => void generateActivationLink()}
            disabled={generatingLink}
          >
            {generatingLink ? 'Generating…' : 'Pending · Generate new link'}
          </button>
        )}
        {user.activationStatus === 'ACTIVATED' && user.enabled && (
          <button
            type="button"
            className="btn-secondary"
            onClick={() => void generatePasswordResetLink()}
            disabled={generatingResetLink}
          >
            {generatingResetLink ? 'Generating…' : 'Reset password'}
          </button>
        )}
      </div>

      <ConnectionsSection userId={user.id} onError={setError} />

      {editing && (
        <EditUserModal
          user={user}
          onClose={() => setEditing(false)}
          onSaved={() => {
            setEditing(false)
            void AdminApi.getUser(user.id).then(setUser)
          }}
        />
      )}

      {newActivation && (
        <div className="admin-modal-backdrop" onClick={() => setNewActivation(null)}>
          <ActivationLinkCard
            username={user.username}
            activationToken={newActivation.activationToken}
            expiresAt={newActivation.expiresAt}
            onDone={() => setNewActivation(null)}
          />
        </div>
      )}

      {newPasswordReset && (
        <div className="admin-modal-backdrop" onClick={() => setNewPasswordReset(null)}>
          <PasswordResetLinkCard
            username={user.username}
            passwordResetToken={newPasswordReset.passwordResetToken}
            expiresAt={newPasswordReset.expiresAt}
            onDone={() => setNewPasswordReset(null)}
          />
        </div>
      )}
    </div>
  )
}
