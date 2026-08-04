import { ApiClient } from '../../../shared/api/ApiClient'

function getDrawingImage(canvasId: string, drawingId: string): Promise<Blob> {
  return ApiClient.getBlob(`/api/me/canvases/${canvasId}/drawings/${drawingId}`)
}

type CreateDrawingResponseDTO = {
  drawingId: string
}

function saveDrawing(canvasId: string, png: Blob): Promise<CreateDrawingResponseDTO> {
  return ApiClient.postBinary<CreateDrawingResponseDTO>(
    `/api/me/canvases/${canvasId}/drawings`,
    png,
    'image/png',
  )
}

export const DrawingsApi = {
  getDrawingImage,
  saveDrawing,
}
