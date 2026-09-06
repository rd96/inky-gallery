import { useState } from 'react'
import type { ReceivedCanvasMessage } from '../../features/messages/types'
import { cardAspectRatio } from './cardAspectRatio'
import { formatCardDate } from './formatCardDate'
import MessageDetailModal from './MessageDetailModal'

type MessageCardProps = {
  message: ReceivedCanvasMessage
}

export default function MessageCard({ message }: MessageCardProps) {
  const [showDetail, setShowDetail] = useState(false)

  return (
    <li
      className="message-card-item"
      style={{ aspectRatio: cardAspectRatio(message.widthPx, message.heightPx) }}
    >
      <button type="button" className="message-card" onClick={() => setShowDetail(true)}>
        <span className="message-card-seal" aria-hidden="true">
          ✉
        </span>
        <span className="message-card-label">From {message.fromDisplayName}</span>
        <span className="card-hover-action">Open</span>
        <div className="card-meta">
          <span>
            {message.widthPx} × {message.heightPx}
          </span>
          <span>{formatCardDate(message.sentAt)}</span>
        </div>
      </button>
      {showDetail && <MessageDetailModal message={message} onClose={() => setShowDetail(false)} />}
    </li>
  )
}
