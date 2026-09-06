import { useEffect, useState } from 'react'
import { MessagesApi } from '../../features/messages/api/messagesApi'
import type { ReceivedCanvasMessage } from '../../features/messages/types'
import { useEscapeKey } from '../../shared/hooks/useEscapeKey'
import { cardAspectRatio } from './cardAspectRatio'
import { formatCardDate } from './formatCardDate'

type MessageDetailModalProps = {
  message: ReceivedCanvasMessage
  onClose: () => void
}

export default function MessageDetailModal({ message, onClose }: MessageDetailModalProps) {
  useEscapeKey(onClose)

  const [imageUrl, setImageUrl] = useState<string | null>(null)
  const [failed, setFailed] = useState(false)

  // A canvas can hold multiple saved revisions - show the most recent one.
  const latestDrawing = message.drawings.reduce((latest, drawing) =>
    drawing.position > latest.position ? drawing : latest,
  )

  useEffect(() => {
    let cancelled = false
    let objectUrl: string | null = null

    MessagesApi.getMessageDrawingImage(message.messageId, latestDrawing.drawingId)
      .then(blob => {
        if (cancelled) return
        objectUrl = URL.createObjectURL(blob)
        setImageUrl(objectUrl)
      })
      .catch(() => {
        if (!cancelled) setFailed(true)
      })

    return () => {
      cancelled = true
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [message.messageId, latestDrawing.drawingId])

  return (
    <div className="admin-modal-backdrop" onClick={onClose}>
      <div
        className="modal-card canvas-preview-modal"
        onClick={e => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-label="Received drawing"
      >
        {imageUrl ? (
          <img
            // Freshly mounted the instant imageUrl is set, so the reveal
            // animation plays exactly once as it swaps in for the placeholder.
            className="canvas-preview-image message-reveal-image"
            src={imageUrl}
            alt=""
            style={{ aspectRatio: cardAspectRatio(message.widthPx, message.heightPx) }}
          />
        ) : (
          <div
            className={
              failed ? 'drawing-card-placeholder message-detail-loading' : 'drawing-card-placeholder message-detail-loading message-detail-loading-active'
            }
            style={{ aspectRatio: cardAspectRatio(message.widthPx, message.heightPx) }}
          >
            {failed ? (
              <span className="drawing-card-error">Failed to load</span>
            ) : (
              <div className="message-detail-loading-content">
                <span className="message-detail-loading-icon" aria-hidden="true">
                  ✉
                </span>
                <span className="message-detail-loading-text">Opening…</span>
              </div>
            )}
          </div>
        )}
        <p className="message-detail-from">
          From {message.fromDisplayName} (@{message.fromUsername})
        </p>
        {message.message && <p className="message-detail-text">“{message.message}”</p>}
        <div className="canvas-preview-meta">
          <span>
            {message.widthPx} × {message.heightPx}
          </span>
          <span>{formatCardDate(message.sentAt)}</span>
        </div>
        <div className="modal-actions">
          <button type="button" className="btn-secondary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  )
}
