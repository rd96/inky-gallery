import { useEffect, useState } from 'react'
import { AdminApi } from '../../features/admin/api/adminApi'
import type { AdminUser } from '../../features/admin/types'
import { useEscapeKey } from '../../shared/hooks/useEscapeKey'

type UserTypeaheadProps = {
  excludeUserIds: string[]
  submitting: boolean
  onSelect: (user: AdminUser) => void
  onCancel: () => void
}

export default function UserTypeahead({
  excludeUserIds,
  submitting,
  onSelect,
  onCancel,
}: UserTypeaheadProps) {
  const [query, setQuery] = useState('')
  const [debouncedQuery, setDebouncedQuery] = useState('')
  const [results, setResults] = useState<AdminUser[]>([])
  const [searching, setSearching] = useState(false)

  useEscapeKey(onCancel)

  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedQuery(query.trim()), 300)
    return () => clearTimeout(timeout)
  }, [query])

  useEffect(() => {
    if (debouncedQuery === '') {
      setResults([])
      return
    }

    let cancelled = false
    setSearching(true)

    AdminApi.searchUsers({ nameSearch: debouncedQuery, page: 1 })
      .then(result => {
        if (cancelled) return
        setResults(result.users.filter(user => !excludeUserIds.includes(user.id)))
      })
      .finally(() => {
        if (!cancelled) setSearching(false)
      })

    return () => {
      cancelled = true
    }
  }, [debouncedQuery, excludeUserIds])

  return (
    <div className="user-typeahead">
      <div className="user-typeahead-input-row">
        <input
          type="search"
          value={query}
          onChange={e => setQuery(e.target.value)}
          placeholder="Search users…"
          autoFocus
          disabled={submitting}
        />
        <button type="button" className="btn-secondary" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
      </div>

      {debouncedQuery !== '' && (
        <ul className="user-typeahead-results">
          {searching && <li className="user-typeahead-status">Searching…</li>}
          {!searching && results.length === 0 && (
            <li className="user-typeahead-status">No matches.</li>
          )}
          {results.map(user => (
            <li key={user.id}>
              <button
                type="button"
                className="user-typeahead-result"
                onClick={() => onSelect(user)}
                disabled={submitting}
              >
                <span className="user-cell-names">
                  <span className="user-cell-displayname">{user.displayName}</span>
                  <span className="user-cell-username">{user.username}</span>
                </span>
                {!user.enabled && (
                  <span className="status-badge status-badge--disabled">Disabled</span>
                )}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
