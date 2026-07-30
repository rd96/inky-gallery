import type { CanvasPath } from 'react-sketch-canvas'

export const DRAFT_SLOT_COUNT = 3

// Namespaced per user so a shared device keeps everyone's in-progress
// drawings separate - switching users just switches which keys get read,
// with no need to clear anything on logout.
export function getDraftStorageKey(userId: string, slot: number): string {
  return `inky-gallery:draw-in-progress:${userId}:${slot}`
}

export function loadDraftPaths(userId: string, slot: number): CanvasPath[] {
  const saved = localStorage.getItem(getDraftStorageKey(userId, slot))
  if (!saved) return []

  try {
    return JSON.parse(saved) as CanvasPath[]
  } catch {
    return []
  }
}

export type DraftSlot = {
  slot: number
  paths: CanvasPath[]
}

export function loadAllDrafts(userId: string): DraftSlot[] {
  const drafts: DraftSlot[] = []
  for (let slot = 0; slot < DRAFT_SLOT_COUNT; slot += 1) {
    const paths = loadDraftPaths(userId, slot)
    if (paths.length > 0) drafts.push({ slot, paths })
  }
  return drafts
}

export function findEmptyDraftSlot(userId: string): number | null {
  for (let slot = 0; slot < DRAFT_SLOT_COUNT; slot += 1) {
    if (loadDraftPaths(userId, slot).length === 0) return slot
  }
  return null
}
