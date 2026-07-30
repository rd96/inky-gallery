import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { DrawingsApi } from '../../features/drawings/api/drawingsApi'
import { findEmptyDraftSlot, loadAllDrafts } from '../../features/drawings/draftStorage'
import type { DrawingMetadata } from '../../features/drawings/types'
import { useAuth } from '../../features/auth/useAuth'
import { formatApiError } from '../../shared/api/ApiError'
import DrawingGrid from './DrawingGrid'
import './GalleryPage.css'

export default function GalleryPage() {
  const { auth } = useAuth()
  // GalleryPage only ever renders inside RequireAuth, so this is always
  // populated in practice; the fallback just satisfies the type checker.
  const userId = auth.status === 'authenticated' ? auth.user.userId : ''

  const [drafts] = useState(() => loadAllDrafts(userId))
  const [emptySlot] = useState(() => findEmptyDraftSlot(userId))
  const [drawings, setDrawings] = useState<DrawingMetadata[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadDrawings = useCallback(async () => {
    setLoading(true)
    setError(null)

    try {
      const result = await DrawingsApi.getMyDrawings()
      setDrawings(result)
    } catch (cause) {
      setError(formatApiError(cause))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadDrawings()
  }, [loadDrawings])

  if (!loading && !error && drafts.length === 0 && drawings.length === 0) {
    return (
      <div className="gallery-empty">
        <p className="gallery-empty-message">You don't have any drawings yet.</p>
        <Link to={`/draw/${emptySlot ?? 0}`} className="gallery-empty-cta">
          Start new drawing
        </Link>
      </div>
    )
  }

  return (
    <div className="gallery-page">
      <div className="gallery-toolbar">
        <h1>Gallery</h1>
        {emptySlot !== null ? (
          <Link to={`/draw/${emptySlot}`} className="gallery-new-drawing">
            New drawing
          </Link>
        ) : (
          <span
            className="gallery-new-drawing gallery-new-drawing--disabled"
            title="All 3 draft slots are full - finish or clear one first"
          >
            New drawing
          </span>
        )}
      </div>

      {error && (
        <p className="gallery-error" role="alert">
          {error}
        </p>
      )}

      <DrawingGrid drafts={drafts} drawings={drawings} />

      {loading && <p>Loading…</p>}
    </div>
  )
}
