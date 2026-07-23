import { useCallback, useEffect, useState } from 'react'
import { AdminApi } from '../../features/admin/api/adminApi'
import type { UserConnections } from '../../features/admin/types'
import { formatApiError } from '../../shared/api/ApiError'
import ConnectionColumn from './ConnectionColumn'

type ConnectionsSectionProps = {
  userId: string
  onError: (message: string) => void
}

export default function ConnectionsSection({ userId, onError }: ConnectionsSectionProps) {
  const [connections, setConnections] = useState<UserConnections | null>(null)
  const [loading, setLoading] = useState(true)

  const loadConnections = useCallback(async () => {
    try {
      const result = await AdminApi.getUserConnections(userId)
      setConnections(result)
    } catch (cause) {
      onError(formatApiError(cause))
    } finally {
      setLoading(false)
    }
  }, [userId, onError])

  useEffect(() => {
    void loadConnections()
  }, [loadConnections])

  async function handleAdd(senderUserId: string, recipientUserId: string) {
    await AdminApi.createConnection({ senderUserId, recipientUserId })
    await loadConnections()
  }

  async function handleRemove(connectionId: string) {
    await AdminApi.deleteConnection(connectionId)
    await loadConnections()
  }

  if (loading || !connections) {
    return (
      <div className="connections-section">
        <h2>Connections</h2>
        <p>Loading…</p>
      </div>
    )
  }

  return (
    <div className="connections-section">
      <h2>Connections</h2>
      <div className="connections-grid">
        <ConnectionColumn
          title="Sends to"
          connections={connections.recipients}
          addLabel="Add recipient"
          excludeUserIds={[userId, ...connections.recipients.map(c => c.userId)]}
          onAdd={selectedUserId => handleAdd(userId, selectedUserId)}
          onRemove={handleRemove}
        />
        <ConnectionColumn
          title="Receives from"
          connections={connections.senders}
          addLabel="Add sender"
          excludeUserIds={[userId, ...connections.senders.map(c => c.userId)]}
          onAdd={selectedUserId => handleAdd(selectedUserId, userId)}
          onRemove={handleRemove}
        />
      </div>
    </div>
  )
}
