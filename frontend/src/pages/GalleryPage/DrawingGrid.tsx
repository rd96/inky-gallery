import type { DraftSlot } from '../../features/drawings/draftStorage'
import type { DrawingMetadata } from '../../features/drawings/types'
import DraftCard from './DraftCard'
import DrawingCard from './DrawingCard'

type DrawingGridProps = {
  drafts: DraftSlot[]
  drawings: DrawingMetadata[]
}

export default function DrawingGrid({ drafts, drawings }: DrawingGridProps) {
  if (drafts.length === 0 && drawings.length === 0) return null

  return (
    <ul className="drawing-grid">
      {drafts.map(draft => (
        <DraftCard key={draft.slot} slot={draft.slot} paths={draft.paths} />
      ))}
      {drawings.map(drawing => (
        <DrawingCard key={drawing.drawingId} drawing={drawing} />
      ))}
    </ul>
  )
}
