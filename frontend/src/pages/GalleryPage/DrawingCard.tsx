import { useEffect, useState } from 'react'
import { DrawingsApi } from '../../features/drawings/api/drawingsApi'
import type { Canvas } from '../../features/drawings/types'
import { cardAspectRatio } from './cardAspectRatio'
import { formatCardDate } from './formatCardDate'

type DrawingCardProps = {
  canvas: Canvas
}

export default function DrawingCard({ canvas }: DrawingCardProps) {
  const [imageUrl, setImageUrl] = useState<string | null>(null)
  const [failed, setFailed] = useState(false)

  // A canvas can hold multiple saved revisions - show the most recent one.
  const latestDrawing = canvas.drawings.reduce((latest, drawing) =>
    drawing.position > latest.position ? drawing : latest,
  )

  useEffect(() => {
    let cancelled = false
    let objectUrl: string | null = null

    setImageUrl(null)
    setFailed(false)

    DrawingsApi.getDrawingImage(canvas.canvasId, latestDrawing.drawingId)
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
  }, [canvas.canvasId, latestDrawing.drawingId])

  return (
    <li
      className="drawing-card"
      style={{ aspectRatio: cardAspectRatio(canvas.widthPx, canvas.heightPx) }}
    >
      {imageUrl && <img className="drawing-card-image" src={imageUrl} alt="" />}
      {!imageUrl && (
        <div className="drawing-card-placeholder">
          {failed && <span className="drawing-card-error">Failed to load</span>}
        </div>
      )}
      <div className="card-meta">
        <span>
          {canvas.widthPx} × {canvas.heightPx}
        </span>
        <span>{formatCardDate(latestDrawing.createdAt)}</span>
      </div>
    </li>
  )
}
