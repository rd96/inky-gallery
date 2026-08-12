import { useState } from 'react'
import type { Orientation } from '../../features/devices/types'
import SendCanvasModal from '../../shared/components/SendCanvasModal'
import { useEscapeKey } from '../../shared/hooks/useEscapeKey'
import { cardAspectRatio } from './cardAspectRatio'
import { formatCardDate } from './formatCardDate'

type CanvasPreviewModalProps = {
  canvasId: string
  deviceModelId?: string
  orientation: Orientation
  imageUrl: string
  widthPx: number
  heightPx: number
  createdAt: string
  onClose: () => void
}

export default function CanvasPreviewModal({
  canvasId,
  deviceModelId,
  orientation,
  imageUrl,
  widthPx,
  heightPx,
  createdAt,
  onClose,
}: CanvasPreviewModalProps) {
  const [showSendModal, setShowSendModal] = useState(false)

  // Disabled while the send modal is showing so Escape backs out of that
  // step instead of also closing the preview underneath it.
  useEscapeKey(onClose, !showSendModal)

  if (showSendModal) {
    return (
      <SendCanvasModal
        canvasId={canvasId}
        deviceModelId={deviceModelId}
        orientation={orientation}
        onClose={() => setShowSendModal(false)}
        onSent={onClose}
      />
    )
  }

  return (
    <div className="admin-modal-backdrop" onClick={onClose}>
      <div
        className="modal-card canvas-preview-modal"
        onClick={e => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-label="Drawing preview"
      >
        <img
          className="canvas-preview-image"
          src={imageUrl}
          alt=""
          style={{ aspectRatio: cardAspectRatio(widthPx, heightPx) }}
        />
        <div className="canvas-preview-meta">
          <span>
            {widthPx} × {heightPx}
          </span>
          <span>{formatCardDate(createdAt)}</span>
        </div>
        <div className="modal-actions">
          <button type="button" className="btn-primary" onClick={() => setShowSendModal(true)}>
            Send
          </button>
          <button type="button" className="btn-secondary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  )
}
