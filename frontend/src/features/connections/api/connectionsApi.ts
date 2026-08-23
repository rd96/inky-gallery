import { ApiClient } from '../../../shared/api/ApiClient'
import type { Connections } from '../types'

function getMyConnections(): Promise<Connections> {
  return ApiClient.get<Connections>('/api/me/connections')
}

export const ConnectionsApi = {
  getMyConnections,
}
