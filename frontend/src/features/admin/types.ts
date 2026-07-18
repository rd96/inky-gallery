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
