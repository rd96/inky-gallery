import { ApiClient } from '../../../shared/api/ApiClient'
import type { Orientation } from '../../devices/types'
import type { Canvas, CanvasDetail, CanvasStatus, CanvasType, DrawingMetadata } from '../types'

type DrawingMetadataResponseDTO = {
  drawingId: string
  position: number
  createdAt: string
}

type CanvasResponseDTO = {
  canvasId: string
  orientation: Orientation
  widthPx: number
  heightPx: number
  status: CanvasStatus
  type: CanvasType
  drawings: DrawingMetadataResponseDTO[]
  sentTo: string[]
  canSendTo: string[]
  createdAt: string
}

function toDrawingMetadata(dto: DrawingMetadataResponseDTO): DrawingMetadata {
  return {
    drawingId: dto.drawingId,
    position: dto.position,
    createdAt: dto.createdAt,
  }
}

function toCanvas(dto: CanvasResponseDTO): Canvas {
  return {
    canvasId: dto.canvasId,
    orientation: dto.orientation,
    widthPx: dto.widthPx,
    heightPx: dto.heightPx,
    status: dto.status,
    type: dto.type,
    drawings: dto.drawings.map(toDrawingMetadata),
    sentTo: dto.sentTo,
    canSendTo: dto.canSendTo,
    createdAt: dto.createdAt,
  }
}

async function queryMyCanvases(): Promise<Canvas[]> {
  const response = await ApiClient.query<CanvasResponseDTO[], { canvasStatus: null }>(
    '/api/me/canvases',
    { canvasStatus: null },
  )
  return response.map(toCanvas)
}

type CanvasDetailResponseDTO = {
  deviceModelId: string
  orientation: Orientation
  widthPx: number
  heightPx: number
  status: CanvasStatus
  type: CanvasType
  palette?: string[] | null
}

function toCanvasDetail(dto: CanvasDetailResponseDTO): CanvasDetail {
  return {
    deviceModelId: dto.deviceModelId,
    orientation: dto.orientation,
    widthPx: dto.widthPx,
    heightPx: dto.heightPx,
    status: dto.status,
    type: dto.type,
    palette: dto.palette ?? null,
  }
}

async function getMyCanvas(canvasId: string): Promise<CanvasDetail> {
  const response = await ApiClient.get<CanvasDetailResponseDTO>(`/api/me/canvases/${canvasId}`)
  return toCanvasDetail(response)
}

type CreateCanvasRequest = {
  targetDeviceModelId: string
  orientation: Orientation
  canvasType: CanvasType
}

type CreateCanvasResponseDTO = {
  canvasId: string
}

function createCanvas(request: CreateCanvasRequest): Promise<CreateCanvasResponseDTO> {
  return ApiClient.post<CreateCanvasResponseDTO, CreateCanvasRequest>('/api/me/canvases', request)
}

type SendCanvasRequest = {
  recipientUserId: string
  message: string | null
  showName: boolean
}

function sendCanvas(canvasId: string, request: SendCanvasRequest): Promise<void> {
  return ApiClient.post<void, SendCanvasRequest>(`/api/me/canvases/${canvasId}/send`, request)
}

export const CanvasesApi = {
  queryMyCanvases,
  getMyCanvas,
  createCanvas,
  sendCanvas,
}
