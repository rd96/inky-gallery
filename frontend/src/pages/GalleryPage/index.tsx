import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { CanvasesApi } from '../../features/drawings/api/canvasesApi'
import type { Canvas } from '../../features/drawings/types'
import { formatApiError } from '../../shared/api/ApiError'
import DrawingGrid from './DrawingGrid'
import NewDraftModal from './NewDraftModal'
import './GalleryPage.css'

export default function GalleryPage() {
  const navigate = useNavigate()

  const [canvases, setCanvases] = useState<Canvas[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showNewDraftModal, setShowNewDraftModal] = useState(false)

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

  useEffect(() => {
    void loadCanvases()
  }, [loadCanvases])

  function handleCreated(canvasId: string) {
    setShowNewDraftModal(false)
    navigate(`/draw/${canvasId}`)
  }

  const drafts = canvases.filter(canvas => canvas.status === 'DRAFT')
  const drawings = canvases.filter(canvas => canvas.status === 'FINISHED')

  if (!loading && !error && canvases.length === 0) {
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
        <button
          type="button"
          className="gallery-new-drawing"
          onClick={() => setShowNewDraftModal(true)}
        >
          New drawing
        </button>
      </div>

      {error && (
        <p className="gallery-error" role="alert">
          {error}
        </p>
      )}

      <DrawingGrid drafts={drafts} drawings={drawings} />

      {loading && <p>Loading…</p>}

      {showNewDraftModal && (
        <NewDraftModal onClose={() => setShowNewDraftModal(false)} onCreated={handleCreated} />
      )}
    </div>
  )
}
