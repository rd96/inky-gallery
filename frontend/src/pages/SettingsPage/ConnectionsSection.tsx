import { useEffect, useState } from 'react'
import { ConnectionsApi } from '../../features/connections/api/connectionsApi'
import type { Connection, Connections } from '../../features/connections/types'
import { formatApiError } from '../../shared/api/ApiError'

function ConnectionList({ title, connections }: { title: string; connections: Connection[] }) {
  return (
    <div className="connection-column">
      <h3>{title}</h3>
      {connections.length === 0 ? (
        <p className="connection-column-empty">None yet.</p>
      ) : (
        <ul className="connection-list">
          {connections.map(connection => (
            <li key={connection.userId} className="connection-list-item">
              <div className="user-cell">
                <div className="user-cell-names">
                  <span className="user-cell-displayname">{connection.displayName}</span>
                  <span className="user-cell-username">{connection.username}</span>
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default function ConnectionsSection() {
  const [connections, setConnections] = useState<Connections | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    ConnectionsApi.getMyConnections()
      .then(setConnections)
      .catch(cause => setError(formatApiError(cause)))
      .finally(() => setLoading(false))
  }, [])

  return (
    <section className="settings-section">
      <div className="settings-section-header">
        <h2>Connections</h2>
      </div>

      {error && (
        <p className="admin-error" role="alert">
          {error}
        </p>
      )}

      {loading ? (
        <p>Loading…</p>
      ) : (
        connections && (
          <div className="connections-grid">
            <ConnectionList title="Sends to" connections={connections.recipients} />
            <ConnectionList title="Receives from" connections={connections.senders} />
          </div>
        )
      )}
    </section>
  )
}
