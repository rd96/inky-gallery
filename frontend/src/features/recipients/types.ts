import type { Orientation } from '../devices/types'

export type Recipient = {
  userId: string
  username: string
  displayName: string
}

export type RecipientDevice = {
  deviceModelId: string
  deviceModelName: string
  landscapeWidthPx: number
  landscapeHeightPx: number
  orientation: Orientation
  palette: string[] | null
}
