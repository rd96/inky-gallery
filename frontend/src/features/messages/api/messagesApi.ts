import { ApiClient } from '../../../shared/api/ApiClient'
import type { DrawingMetadata } from '../../drawings/types'
import type { ReceivedCanvasMessage } from '../types'

type DrawingMetadataResponseDTO = {
  drawingId: string
  position: number
  createdAt: string
}

type ReceivedCanvasMessageResponseDTO = {
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
  drawings: DrawingMetadataResponseDTO[]
}

function toDrawingMetadata(dto: DrawingMetadataResponseDTO): DrawingMetadata {
  return {
    drawingId: dto.drawingId,
    position: dto.position,
    createdAt: dto.createdAt,
  }
}

function toReceivedCanvasMessage(dto: ReceivedCanvasMessageResponseDTO): ReceivedCanvasMessage {
  return {
    messageId: dto.messageId,
    fromUserId: dto.fromUserId,
    fromDisplayName: dto.fromDisplayName,
    fromUsername: dto.fromUsername,
    message: dto.message,
    showName: dto.showName,
    sentAt: dto.sentAt,
    canvasId: dto.canvasId,
    widthPx: dto.widthPx,
    heightPx: dto.heightPx,
    drawings: dto.drawings.map(toDrawingMetadata),
  }
}

async function getReceivedMessages(): Promise<ReceivedCanvasMessage[]> {
  const response = await ApiClient.get<ReceivedCanvasMessageResponseDTO[]>('/api/me/messages')
  return response.map(toReceivedCanvasMessage)
}

function getMessageDrawingImage(messageId: string, drawingId: string): Promise<Blob> {
  return ApiClient.getBlob(`/api/me/messages/${messageId}/drawings/${drawingId}`)
}

export const MessagesApi = {
  getReceivedMessages,
  getMessageDrawingImage,
}
