import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { CanvasesApi } from '../../features/drawings/api/canvasesApi'
import type { Canvas } from '../../features/drawings/types'
import { MessagesApi } from '../../features/messages/api/messagesApi'
import type { ReceivedCanvasMessage } from '../../features/messages/types'
import { formatApiError } from '../../shared/api/ApiError'
import DrawingGrid from './DrawingGrid'
import './GalleryPage.css'
import MessageGrid from './MessageGrid'
import NewDraftModal from './NewDraftModal'

export default function GalleryPage() {
  const navigate = useNavigate()

  const [canvases, setCanvases] = useState<Canvas[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showNewDraftModal, setShowNewDraftModal] = useState(false)

  const [messages, setMessages] = useState<ReceivedCanvasMessage[]>([])
  const [messagesLoading, setMessagesLoading] = useState(true)
  const [messagesError, setMessagesError] = useState<string | null>(null)
  const [showReceived, setShowReceived] = useState(true)

  const loadCanvases = useCallback(async () => {
    setLoading(true)
    setError(null)

    try {
      const result = await CanvasesApi.queryMyCanvases()
      setCanvases(result)
    } catch (cause) {
      setError(formatApiError(cause))
    } finally {
      setLoading(false)
    }
  }, [])

  const loadMessages = useCallback(async () => {
    setMessagesLoading(true)
    setMessagesError(null)

    try {
      const result = await MessagesApi.getReceivedMessages()
      setMessages(result)
    } catch (cause) {
      setMessagesError(formatApiError(cause))
    } finally {
      setMessagesLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadCanvases()
    void loadMessages()
  }, [loadCanvases, loadMessages])

  function handleCreated(canvasId: string) {
    setShowNewDraftModal(false)
    navigate(`/draw/${canvasId}`)
  }

  const drafts = canvases.filter(canvas => canvas.status === 'DRAFT')
  const drawings = canvases.filter(canvas => canvas.status === 'FINISHED')

  const nothingToShow =
    !loading && !error && canvases.length === 0 && !messagesLoading && !messagesError && messages.length === 0

  if (nothingToShow) {
    return (
      <div className="gallery-empty">
        <p className="gallery-empty-message">You don't have any drawings yet.</p>
        <button
          type="button"
          className="gallery-empty-cta"
          onClick={() => setShowNewDraftModal(true)}
        >
          Start new drawing
        </button>
        {showNewDraftModal && (
          <NewDraftModal onClose={() => setShowNewDraftModal(false)} onCreated={handleCreated} />
        )}
      </div>
    )
  }

  return (
    <div className="gallery-page">
      <div className="gallery-toolbar">
        <h1>Gallery</h1>
        <div className="gallery-toolbar-actions">
          {messages.length > 0 && (
            <label className="mail-toggle">
              <input
                type="checkbox"
                className="mail-toggle-input"
                checked={showReceived}
                onChange={event => setShowReceived(event.target.checked)}
              />
              <span className="mail-toggle-track" aria-hidden="true">
                <span className="mail-toggle-thumb">✉</span>
              </span>
              <span className="mail-toggle-text">Show received</span>
              <span className="mail-toggle-count">{messages.length}</span>
            </label>
          )}
          <button
            type="button"
            className="gallery-new-drawing"
            onClick={() => setShowNewDraftModal(true)}
          >
            New drawing
          </button>
        </div>
      </div>

      {error && (
        <p className="gallery-error" role="alert">
          {error}
        </p>
      )}

      {canvases.length === 0 && !loading ? (
        <p className="gallery-section-empty">You don't have any drawings yet.</p>
      ) : (
        <DrawingGrid drafts={drafts} drawings={drawings} />
      )}

      {loading && <p>Loading…</p>}

      {messagesError && (
        <p className="gallery-error" role="alert">
          {messagesError}
        </p>
      )}

      {showReceived && messages.length > 0 && (
        <section className="received-section">
          <h2 className="received-section-heading">
            <span className="received-section-icon" aria-hidden="true">
              ✉
            </span>
            Received from friends
          </h2>
          <MessageGrid messages={messages} />
        </section>
      )}

      {showNewDraftModal && (
        <NewDraftModal onClose={() => setShowNewDraftModal(false)} onCreated={handleCreated} />
      )}
    </div>
  )
}
