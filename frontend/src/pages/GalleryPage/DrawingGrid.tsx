import type { CanvasPath } from 'react-sketch-canvas'
import type { DrawingMetadata } from '../../features/drawings/types'
import DraftCard from './DraftCard'
import DrawingCard from './DrawingCard'

type DrawingGridProps = {
  draftPaths: CanvasPath[]
  drawings: DrawingMetadata[]
}

export default function DrawingGrid({ draftPaths, drawings }: DrawingGridProps) {
  if (draftPaths.length === 0 && drawings.length === 0) return null

  return (
    <ul className="drawing-grid">
      {draftPaths.length > 0 && <DraftCard paths={draftPaths} />}
      {drawings.map(drawing => (
        <DrawingCard key={drawing.drawingId} drawing={drawing} />
      ))}
    </ul>
  )
}
