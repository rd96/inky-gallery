import { ApiClient } from '../../../shared/api/ApiClient'
import type { Orientation, UserDevice } from '../types'

type UserDeviceResponseDTO = {
  deviceId: string
  modelName: string
  deviceNickname: string
  widthPx: number
  heightPx: number
  orientation: Orientation
  palette?: string[] | null
  enabled: boolean
}

function toUserDevice(dto: UserDeviceResponseDTO): UserDevice {
  return {
    deviceId: dto.deviceId,
    deviceNickname: dto.deviceNickname,
    modelName: dto.modelName,
    widthPx: dto.widthPx,
    heightPx: dto.heightPx,
    orientation: dto.orientation,
    palette: dto.palette ?? null,
    enabled: dto.enabled,
  }
}

async function getMyDevices(): Promise<UserDevice[]> {
  const response = await ApiClient.get<UserDeviceResponseDTO[]>('/api/me/devices')
  return response.map(toUserDevice)
}

type RegisterDeviceRequest = {
  deviceNickname: string
  deviceModelId: string
  orientation: Orientation
}

function registerDevice(request: RegisterDeviceRequest): Promise<void> {
  return ApiClient.post<void, RegisterDeviceRequest>('/api/me/devices', request)
}

export type UpdateDeviceRequest = {
  deviceNickname?: string
  orientation?: Orientation
  enabled?: boolean
}

function updateDevice(deviceId: string, patch: UpdateDeviceRequest): Promise<void> {
  return ApiClient.patch<void, UpdateDeviceRequest>(`/api/me/devices/${deviceId}`, patch)
}

export const MyDevicesApi = {
  getMyDevices,
  registerDevice,
  updateDevice,
}
