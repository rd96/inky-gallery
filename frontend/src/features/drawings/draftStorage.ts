import type { CanvasPath } from 'react-sketch-canvas'

// Namespaced per user so a shared device keeps everyone's in-progress
// drawings separate - switching users just switches which keys get read,
// with no need to clear anything on logout.
export function getDraftStorageKey(userId: string, canvasId: string): string {
  return `inky-gallery:draw-in-progress:${userId}:${canvasId}`
}

export function loadDraftPaths(userId: string, canvasId: string): CanvasPath[] {
  const saved = localStorage.getItem(getDraftStorageKey(userId, canvasId))
  if (!saved) return []

  try {
    return JSON.parse(saved) as CanvasPath[]
  } catch {
    return []
  }
}
