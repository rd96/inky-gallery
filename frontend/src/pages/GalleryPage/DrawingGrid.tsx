import type { Canvas } from '../../features/drawings/types'
import DraftCard from './DraftCard'
import DrawingCard from './DrawingCard'

type DrawingGridProps = {
  drafts: Canvas[]
  drawings: Canvas[]
}

export default function DrawingGrid({ drafts, drawings }: DrawingGridProps) {
  if (drafts.length === 0 && drawings.length === 0) return null

  return (
    <ul className="drawing-grid">
      {drafts.map(canvas => (
        <DraftCard key={canvas.canvasId} canvas={canvas} />
      ))}
      {drawings.map(canvas => (
        <DrawingCard key={canvas.canvasId} canvas={canvas} />
      ))}
    </ul>
  )
}
