import type { DrawingMetadata } from '../drawings/types'

export type ReceivedCanvasMessage = {
  messageId: string
  fromUserId: string
  fromDisplayName: string
  fromUsername: string
  message: string | null
  showName: boolean
  sentAt: string
  canvasId: string
  widthPx: number
  heightPx: number
  drawings: DrawingMetadata[]
}
