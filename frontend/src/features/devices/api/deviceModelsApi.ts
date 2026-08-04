import { ApiClient } from '../../../shared/api/ApiClient'
import type { DeviceModel } from '../types'

type DeviceModelResponseDTO = {
  deviceModelId: string
  deviceName: string
  landscapeWidthPx: number
  landscapeHeightPx: number
  palette?: string[] | null
}

function toDeviceModel(dto: DeviceModelResponseDTO): DeviceModel {
  return {
    deviceModelId: dto.deviceModelId,
    deviceName: dto.deviceName,
    landscapeWidthPx: dto.landscapeWidthPx,
    landscapeHeightPx: dto.landscapeHeightPx,
    palette: dto.palette ?? null,
  }
}

async function getDeviceModels(): Promise<DeviceModel[]> {
  const response = await ApiClient.get<DeviceModelResponseDTO[]>('/api/search/device-models')
  return response.map(toDeviceModel)
}

export const DeviceModelsApi = {
  getDeviceModels,
}
