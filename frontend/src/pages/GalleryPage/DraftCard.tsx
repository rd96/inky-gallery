import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { ReactSketchCanvas, type CanvasPath, type ReactSketchCanvasRef } from 'react-sketch-canvas'
import { useAuth } from '../../features/auth/useAuth'
import {
  isDraftUnknownToThisDevice,
  loadDraftPaths,
  UNKNOWN_DRAFT_DEVICE_MESSAGE,
} from '../../features/drawings/draftStorage'
import type { Canvas } from '../../features/drawings/types'
import { cardAspectRatio } from './cardAspectRatio'
import { formatCardDate } from './formatCardDate'

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
  const [isUnknownToThisDevice] = useState(() => isDraftUnknownToThisDevice(userId, canvas.canvasId))

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
    <li
      className="draft-card-item"
      style={{ aspectRatio: cardAspectRatio(canvas.widthPx, canvas.heightPx) }}
    >
      <Link to={`/draw/${canvas.canvasId}`} className="draft-card">
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
        <div className="card-meta">
          <span>
            {canvas.widthPx} × {canvas.heightPx}
          </span>
          <span>{formatCardDate(canvas.createdAt)}</span>
        </div>
      </Link>
      {/* Sibling of the clipped .draft-card so its tooltip isn't cut off by
          the overflow:hidden used there to round the image's corners. */}
      {isUnknownToThisDevice && (
        <span className="draft-card-warning" aria-label={UNKNOWN_DRAFT_DEVICE_MESSAGE}>
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
            <line x1="12" y1="9" x2="12" y2="13" />
            <line x1="12" y1="17" x2="12.01" y2="17" />
          </svg>
          <span className="draft-card-warning-tooltip">{UNKNOWN_DRAFT_DEVICE_MESSAGE}</span>
        </span>
      )}
    </li>
  )
}
