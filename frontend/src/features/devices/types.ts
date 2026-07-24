export type DeviceModel = {
  deviceModelId: string
  deviceName: string
  landscapeWidthPx: number
  landscapeHeightPx: number
  colourSwatch: string[] | null
}

export type Orientation = 'LANDSCAPE' | 'PORTRAIT'

export type UserDevice = {
  deviceId: string
  deviceNickname: string
  modelName: string
  widthPx: number
  heightPx: number
  orientation: Orientation
  colourSwatch: string[] | null
  enabled: boolean
}
