import { useCallback, useEffect, useRef, useState } from 'react'
import { ADMIN_USER_PAGE_SIZE, AdminApi } from '../../features/admin/api/adminApi'
import type { ActivationStatus, AdminUser } from '../../features/admin/types'
import type { Role } from '../../features/auth/types'
import CreateUserForm from './CreateUserForm'
import UserList from './UserList'
import './AdminPage.css'

export default function AdminPage() {
  const [search, setSearch] = useState('')
  const [debouncedSearch, setDebouncedSearch] = useState('')
  const [roleFilter, setRoleFilter] = useState<Role | ''>('')
  const [activationStatusFilter, setActivationStatusFilter] = useState<
    ActivationStatus | ''
  >('')
  const [enabledFilter, setEnabledFilter] = useState<'' | 'true' | 'false'>('')
  const [page, setPage] = useState(1)
  const [users, setUsers] = useState<AdminUser[]>([])
  const [totalCount, setTotalCount] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedSearch(search), 300)
    return () => clearTimeout(timeout)
  }, [search])

  const loadUsers = useCallback(
    async (
      query: string,
      role: Role | '',
      activationStatus: ActivationStatus | '',
      enabled: '' | 'true' | 'false',
      pageNumber: number,
      options?: { silent?: boolean },
    ) => {
      if (!options?.silent) setLoading(true)
      setError(null)

      try {
        const result = await AdminApi.searchUsers({
          nameSearch: query,
          role: role === '' ? undefined : role,
          activationStatus: activationStatus === '' ? undefined : activationStatus,
          enabled: enabled === '' ? undefined : enabled === 'true',
          page: pageNumber,
        })
        setUsers(result.users)
        setTotalCount(result.totalCount)
      } catch {
        setError('Unable to load users. Please try again.')
      } finally {
        if (!options?.silent) setLoading(false)
      }
    },
    [],
  )

  const filtersRef = useRef({
    debouncedSearch,
    roleFilter,
    activationStatusFilter,
    enabledFilter,
  })

  useEffect(() => {
    const filtersChanged =
      filtersRef.current.debouncedSearch !== debouncedSearch ||
      filtersRef.current.roleFilter !== roleFilter ||
      filtersRef.current.activationStatusFilter !== activationStatusFilter ||
      filtersRef.current.enabledFilter !== enabledFilter

    filtersRef.current = {
      debouncedSearch,
      roleFilter,
      activationStatusFilter,
      enabledFilter,
    }

    if (filtersChanged && page !== 1) {
      setPage(1)
      return
    }

    void loadUsers(
      debouncedSearch,
      roleFilter,
      activationStatusFilter,
      enabledFilter,
      page,
    )
  }, [debouncedSearch, roleFilter, activationStatusFilter, enabledFilter, page, loadUsers])

  function refreshUsers() {
    void loadUsers(
      debouncedSearch,
      roleFilter,
      activationStatusFilter,
      enabledFilter,
      page,
      { silent: true },
    )
  }

  const totalPages = Math.max(1, Math.ceil(totalCount / ADMIN_USER_PAGE_SIZE))

  return (
    <div className="admin-page">
      <h1>Users</h1>

      <div className="admin-toolbar">
        <div className="admin-filters">
          <input
            type="search"
            className="admin-search"
            placeholder="Search users…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
          <select
            value={roleFilter}
            onChange={e => setRoleFilter(e.target.value as Role | '')}
          >
            <option value="">All roles</option>
            <option value="ADMIN">Admin</option>
            <option value="USER">User</option>
          </select>
          <select
            value={activationStatusFilter}
            onChange={e =>
              setActivationStatusFilter(e.target.value as ActivationStatus | '')
            }
          >
            <option value="">All activation statuses</option>
            <option value="PENDING">Pending</option>
            <option value="ACTIVATED">Activated</option>
          </select>
          <select
            value={enabledFilter}
            onChange={e =>
              setEnabledFilter(e.target.value as '' | 'true' | 'false')
            }
          >
            <option value="">All enabled statuses</option>
            <option value="true">Enabled</option>
            <option value="false">Disabled</option>
          </select>
        </div>

        <CreateUserForm onCreated={refreshUsers} />
      </div>

      {error && (
        <p className="admin-error" role="alert">
          {error}
        </p>
      )}
      {loading ? (
        <p>Loading…</p>
      ) : (
        <>
          <UserList users={users} onError={setError} />
          {totalPages > 1 && (
            <div className="admin-pagination">
              <button
                type="button"
                disabled={page <= 1}
                onClick={() => setPage(p => p - 1)}
              >
                Previous
              </button>
              <span>
                Page {page} of {totalPages}
              </span>
              <button
                type="button"
                disabled={page >= totalPages}
                onClick={() => setPage(p => p + 1)}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
