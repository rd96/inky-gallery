import type { Orientation } from '../devices/types'

export type DrawingMetadata = {
  drawingId: string
  position: number
  createdAt: string
}

export type CanvasStatus = 'DRAFT' | 'FINISHED'
export type CanvasType = 'SINGLE' | 'STACK'

export type Canvas = {
  canvasId: string
  orientation: Orientation
  widthPx: number
  heightPx: number
  status: CanvasStatus
  type: CanvasType
  drawings: DrawingMetadata[]
  sentTo: string[]
  canSendTo: string[]
  createdAt: string
}

export type CanvasDetail = {
  widthPx: number
  heightPx: number
  status: CanvasStatus
  type: CanvasType
  palette: string[] | null
}
