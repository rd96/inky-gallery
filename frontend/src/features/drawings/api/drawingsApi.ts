import { ApiClient } from '../../../shared/api/ApiClient'
import type { DrawingMetadata } from '../types'

type DrawingMetadataResponseDTO = {
  drawingId: string
  widthPx: number
  heightPx: number
  createdAt: string
}

function toDrawingMetadata(dto: DrawingMetadataResponseDTO): DrawingMetadata {
  return {
    drawingId: dto.drawingId,
    widthPx: dto.widthPx,
    heightPx: dto.heightPx,
    createdAt: dto.createdAt,
  }
}

async function getMyDrawings(): Promise<DrawingMetadata[]> {
  const response = await ApiClient.get<DrawingMetadataResponseDTO[]>('/api/me/drawings')
  return response.map(toDrawingMetadata)
}

function getDrawingImage(drawingId: string): Promise<Blob> {
  return ApiClient.getBlob(`/api/me/drawings/${drawingId}`)
}

type CreateDrawingResponseDTO = {
  drawingId: string
}

function saveDrawing(png: Blob): Promise<CreateDrawingResponseDTO> {
  return ApiClient.postBinary<CreateDrawingResponseDTO>('/api/me/drawings', png, 'image/png')
}

export const DrawingsApi = {
  getMyDrawings,
  getDrawingImage,
  saveDrawing,
}
