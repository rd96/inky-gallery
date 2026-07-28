import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { DrawingsApi } from '../../features/drawings/api/drawingsApi'
import { loadDraftPaths } from '../../features/drawings/draftStorage'
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

  const [draftPaths] = useState(() => loadDraftPaths(userId))
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

  if (!loading && !error && draftPaths.length === 0 && drawings.length === 0) {
    return (
      <div className="gallery-empty">
        <p className="gallery-empty-message">You don't have any drawings yet.</p>
        <Link to="/draw" className="gallery-empty-cta">
          Start new drawing
        </Link>
      </div>
    )
  }

  return (
    <div className="gallery-page">
      <div className="gallery-toolbar">
        <h1>Gallery</h1>
        <Link to="/draw" className="gallery-new-drawing">
          New drawing
        </Link>
      </div>

      {error && (
        <p className="gallery-error" role="alert">
          {error}
        </p>
      )}

      <DrawingGrid draftPaths={draftPaths} drawings={drawings} />

      {loading && <p>Loading…</p>}
    </div>
  )
}
