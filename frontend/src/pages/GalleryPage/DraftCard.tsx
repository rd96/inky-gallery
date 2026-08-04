import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { ReactSketchCanvas, type CanvasPath, type ReactSketchCanvasRef } from 'react-sketch-canvas'
import { useAuth } from '../../features/auth/useAuth'
import { loadDraftPaths } from '../../features/drawings/draftStorage'
import type { Canvas } from '../../features/drawings/types'

type DraftCardProps = {
  canvas: Canvas
}

export default function DraftCard({ canvas }: DraftCardProps) {
  const { auth } = useAuth()
  // GalleryPage only ever renders inside RequireAuth, so this is always
  // populated in practice; the fallback just satisfies the type checker.
  const userId = auth.status === 'authenticated' ? auth.user.userId : ''

  const canvasRef = useRef<ReactSketchCanvasRef>(null)
  const [imageUrl, setImageUrl] = useState<string | null>(null)
  const [paths] = useState(() => loadDraftPaths(userId, canvas.canvasId))

  // ReactSketchCanvas has no prop for initial paths - they must be loaded
  // imperatively via the ref once it's mounted.
  useEffect(() => {
    canvasRef.current?.loadPaths(paths)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Rendering the live canvas straight into the grid clips it - the SVG has
  // no viewBox, so shrinking it to card size cuts off paths instead of
  // scaling them. Exporting a flat PNG from the full-size offscreen canvas
  // and displaying that with object-fit lets it scale down cleanly, the
  // same way saved drawings are shown.
  function handleChange(updatedPaths: CanvasPath[]) {
    if (updatedPaths.length === 0) return
    // Without explicit dimensions, exportImage multiplies by
    // window.devicePixelRatio, doubling output size on Retina displays.
    canvasRef.current
      ?.exportImage('png', { width: canvas.widthPx, height: canvas.heightPx })
      .then(setImageUrl)
      .catch(() => {})
  }

  return (
    <Link
      to={`/draw/${canvas.canvasId}`}
      className="draft-card"
      style={{ aspectRatio: `${canvas.widthPx} / ${canvas.heightPx}` }}
    >
      <div className="draft-card-offscreen" aria-hidden="true">
        <ReactSketchCanvas
          ref={canvasRef}
          width={`${canvas.widthPx}px`}
          height={`${canvas.heightPx}px`}
          canvasColor="white"
          onChange={handleChange}
          readOnly
        />
      </div>
      {imageUrl && <img className="draft-card-image" src={imageUrl} alt="" />}
      <span className="draft-card-label">Continue draft</span>
    </Link>
  )
}
