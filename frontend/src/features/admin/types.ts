import type { Role } from '../auth/types'

export type ActivationStatus = 'PENDING' | 'ACTIVATED'

export type AdminUser = {
  id: string
  username: string
  displayName: string
  role: Role
  activationStatus: ActivationStatus
  enabled: boolean
  createdAt: string
}

export type UserConnection = {
  connectionId: string
  userId: string
  username: string
  displayName: string
  enabled: boolean
}

export type UserConnections = {
  senders: UserConnection[]
  recipients: UserConnection[]
}
