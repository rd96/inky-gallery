import { ApiClient } from '../../../shared/api/ApiClient'
import type { Orientation } from '../../devices/types'
import type { Recipient } from '../types'

type DeviceMatching = {
  deviceModelId: string
  orientation: Orientation
}

type QueryRecipientsRequest = {
  deviceMatching?: DeviceMatching
}

async function searchRecipients(deviceMatching?: DeviceMatching): Promise<Recipient[]> {
  return ApiClient.query<Recipient[], QueryRecipientsRequest>('/api/me/recipients', { deviceMatching })
}

export const RecipientsApi = {
  searchRecipients,
}
