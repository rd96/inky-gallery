import { ApiClient } from '../../../shared/api/ApiClient'

type UpdateDisplayNameRequest = {
  displayName: string
}

function updateDisplayName(displayName: string): Promise<void> {
  return ApiClient.patch<void, UpdateDisplayNameRequest>('/api/me', { displayName })
}

function deleteAccount(): Promise<void> {
  return ApiClient.delete<void>('/api/me')
}

export const SettingsApi = {
  updateDisplayName,
  deleteAccount,
}
