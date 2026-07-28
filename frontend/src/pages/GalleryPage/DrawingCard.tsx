import { useEffect, useState } from 'react'
import { DrawingsApi } from '../../features/drawings/api/drawingsApi'
import type { DrawingMetadata } from '../../features/drawings/types'

type DrawingCardProps = {
  drawing: DrawingMetadata
}

export default function DrawingCard({ drawing }: DrawingCardProps) {
  const [imageUrl, setImageUrl] = useState<string | null>(null)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let cancelled = false
    let objectUrl: string | null = null

    setImageUrl(null)
    setFailed(false)

    DrawingsApi.getDrawingImage(drawing.drawingId)
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
  }, [drawing.drawingId])

  return (
    <li
      className="drawing-card"
      style={{ aspectRatio: `${drawing.widthPx} / ${drawing.heightPx}` }}
    >
      {imageUrl && <img className="drawing-card-image" src={imageUrl} alt="" />}
      {!imageUrl && (
        <div className="drawing-card-placeholder">
          {failed && <span className="drawing-card-error">Failed to load</span>}
        </div>
      )}
    </li>
  )
}
