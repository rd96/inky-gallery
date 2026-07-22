import { useLayoutEffect, useRef, useState } from 'react'
import { AdminApi } from '../../features/admin/api/adminApi'
import type { AdminUser } from '../../features/admin/types'
import type { Role } from '../../features/auth/types'
import { useAuth } from '../../features/auth/useAuth'
import { formatApiError } from '../../shared/api/ApiError'
import { useEscapeKey } from '../../shared/hooks/useEscapeKey'
import { useOutsideClick } from '../../shared/hooks/useOutsideClick'
import EditUserModal from './EditUserModal'

type UserActionsMenuProps = {
  user: AdminUser
  onChanged: () => void
  onError: (message: string) => void
}

export default function UserActionsMenu({
  user,
  onChanged,
  onError,
}: UserActionsMenuProps) {
  const { auth } = useAuth()
  const isSelf = auth.status === 'authenticated' && auth.user.userId === user.id
  const [open, setOpen] = useState(false)
  const [openUpward, setOpenUpward] = useState(false)
  const [togglingEnabled, setTogglingEnabled] = useState(false)
  const [togglingRole, setTogglingRole] = useState(false)
  const [editing, setEditing] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)
  const dropdownRef = useRef<HTMLDivElement>(null)

  useLayoutEffect(() => {
    if (!open) return

    const dropdown = dropdownRef.current
    if (!dropdown) return

    setOpenUpward(dropdown.getBoundingClientRect().bottom > window.innerHeight)
  }, [open])

  useEscapeKey(() => setOpen(false), open)
  useOutsideClick(menuRef, () => setOpen(false), open)

  async function toggleEnabled() {
    setOpen(false)
    setTogglingEnabled(true)

    try {
      await AdminApi.updateUser(user.id, { enabled: !user.enabled })
      onChanged()
    } catch (cause) {
      onError(formatApiError(cause))
    } finally {
      setTogglingEnabled(false)
    }
  }

  async function toggleRole() {
    const nextRole: Role = user.role === 'ADMIN' ? 'USER' : 'ADMIN'
    setOpen(false)
    setTogglingRole(true)

    try {
      await AdminApi.updateUser(user.id, { role: nextRole })
      onChanged()
    } catch (cause) {
      onError(formatApiError(cause))
    } finally {
      setTogglingRole(false)
    }
  }

  const busy = togglingEnabled || togglingRole

  return (
    <div className="user-actions-menu" ref={menuRef}>
      <button
        type="button"
        className="user-actions-trigger"
        onClick={() => setOpen(o => !o)}
        disabled={busy}
        aria-label={`Actions for ${user.username}`}
        aria-haspopup="true"
        aria-expanded={open}
      >
        <span aria-hidden="true">⋮</span>
      </button>

      {open && (
        <div
          className={`user-actions-dropdown ${openUpward ? 'user-actions-dropdown--up' : ''}`}
          role="menu"
          ref={dropdownRef}
        >
          <button
            type="button"
            role="menuitem"
            onClick={() => {
              setOpen(false)
              setEditing(true)
            }}
          >
            Edit
          </button>
          <button
            type="button"
            role="menuitem"
            onClick={toggleRole}
            disabled={isSelf}
            title={isSelf ? "You can't change your own role" : undefined}
          >
            {user.role === 'ADMIN' ? 'Remove admin' : 'Make admin'}
          </button>
          <div className="user-actions-divider" />
          <button
            type="button"
            role="menuitem"
            className={user.enabled ? 'user-actions-danger' : undefined}
            onClick={toggleEnabled}
            disabled={isSelf}
            title={isSelf ? "You can't disable your own account" : undefined}
          >
            {user.enabled ? 'Disable' : 'Enable'}
          </button>
        </div>
      )}

      {editing && (
        <EditUserModal
          user={user}
          onClose={() => setEditing(false)}
          onSaved={() => {
            setEditing(false)
            onChanged()
          }}
        />
      )}
    </div>
  )
}
