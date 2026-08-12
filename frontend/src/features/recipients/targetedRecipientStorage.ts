import type { Recipient } from './types'

// Which recipient a canvas was created for is tracked here, not as part of
// the canvas itself - the backend has no concept of it yet, and this is
// meant to stay a loose, frontend-only default rather than something the
// canvas domain model carries around.
function getTargetedRecipientKey(userId: string, canvasId: string): string {
  return `inky-gallery:targeted-recipient:${userId}:${canvasId}`
}

export function saveTargetedRecipient(userId: string, canvasId: string, recipient: Recipient): void {
  localStorage.setItem(getTargetedRecipientKey(userId, canvasId), JSON.stringify(recipient))
}

export function loadTargetedRecipient(userId: string, canvasId: string): Recipient | null {
  const saved = localStorage.getItem(getTargetedRecipientKey(userId, canvasId))
  if (!saved) return null

  try {
    return JSON.parse(saved) as Recipient
  } catch {
    return null
  }
}
