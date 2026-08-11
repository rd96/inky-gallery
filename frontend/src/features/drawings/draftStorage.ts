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

// Recorded once, at creation time, so a device can later tell whether it was
// the one that started a given draft - a canvas can be edited from multiple
// devices, but only the creating device ever writes this key.
function getCreatedHereKey(userId: string, canvasId: string): string {
  return `inky-gallery:draft-created-here:${userId}:${canvasId}`
}

export function markDraftCreatedOnThisDevice(userId: string, canvasId: string): void {
  localStorage.setItem(getCreatedHereKey(userId, canvasId), '1')
}

export function wasDraftCreatedOnThisDevice(userId: string, canvasId: string): boolean {
  return localStorage.getItem(getCreatedHereKey(userId, canvasId)) !== null
}

// Neither this device's local strokes nor its "created here" marker know
// about this draft - it was most likely started on another device. There's
// no way to know whether that device has unsaved progress, so this is only
// ever a caveat about starting from blank, never a claim about lost work.
export function isDraftUnknownToThisDevice(userId: string, canvasId: string): boolean {
  return !wasDraftCreatedOnThisDevice(userId, canvasId) && loadDraftPaths(userId, canvasId).length === 0
}

export const UNKNOWN_DRAFT_DEVICE_MESSAGE =
  "This device has no record of this draft. If you drew on it elsewhere without saving, that won't appear here - continuing will start from blank."
