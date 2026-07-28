import type { CanvasPath } from 'react-sketch-canvas'

// Namespaced per user so a shared device keeps everyone's in-progress
// drawing separate - switching users just switches which key gets read,
// with no need to clear anything on logout.
export function getDraftStorageKey(userId: string): string {
  return `inky-gallery:draw-in-progress:${userId}`
}

export function loadDraftPaths(userId: string): CanvasPath[] {
  const saved = localStorage.getItem(getDraftStorageKey(userId))
  if (!saved) return []

  try {
    return JSON.parse(saved) as CanvasPath[]
  } catch {
    return []
  }
}
