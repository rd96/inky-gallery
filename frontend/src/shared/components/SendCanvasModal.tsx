import { useEffect, useState } from 'react'
import { useAuth } from '../../features/auth/useAuth'
import type { Orientation } from '../../features/devices/types'
import { CanvasesApi } from '../../features/drawings/api/canvasesApi'
import { RecipientsApi } from '../../features/recipients/api/recipientsApi'
import { loadTargetedRecipient } from '../../features/recipients/targetedRecipientStorage'
import type { Recipient } from '../../features/recipients/types'
import { formatApiError } from '../api/ApiError'
import { useEscapeKey } from '../hooks/useEscapeKey'
import RecipientPicker from './RecipientPicker'
import './SendCanvasModal.css'

const MAX_MESSAGE_LENGTH = 100

type SendCanvasModalProps = {
  canvasId: string
  // Not every canvas response exposes this yet - when it's missing, the
  // recipient search just falls back to showing everyone rather than
  // filtering by device compatibility.
  deviceModelId?: string
  orientation: Orientation
  onClose: () => void
  onSent: () => void
}

type Step = 'recipient' | 'compose'

export default function SendCanvasModal({
  canvasId,
  deviceModelId,
  orientation,
  onClose,
  onSent,
}: SendCanvasModalProps) {
  const { auth } = useAuth()
  // SendCanvasModal only ever renders inside RequireAuth, so this is always
  // populated in practice; the fallback just satisfies the type checker.
  const userId = auth.status === 'authenticated' ? auth.user.userId : ''

  // The canvas may have been created with a recipient already in mind - if
  // so, skip straight to composing a message for them, but leave "Back"
  // available in case they'd rather send it to someone else.
  const targetedRecipient = loadTargetedRecipient(userId, canvasId)

  const [step, setStep] = useState<Step>(targetedRecipient ? 'compose' : 'recipient')
  const [recipients, setRecipients] = useState<Recipient[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)

  const [selectedRecipient, setSelectedRecipient] = useState<Recipient | null>(targetedRecipient)
  const [message, setMessage] = useState('')
  const [showName, setShowName] = useState(true)
  const [sending, setSending] = useState(false)
  const [sendError, setSendError] = useState<string | null>(null)

  useEscapeKey(onClose)

  useEffect(() => {
    let cancelled = false

    RecipientsApi.searchRecipients(deviceModelId ? { deviceModelId, orientation } : undefined)
      .then(result => {
        if (!cancelled) setRecipients(result)
      })
      .catch(cause => {
        if (!cancelled) setLoadError(formatApiError(cause))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [deviceModelId, orientation])

  function handlePickRecipient(recipient: Recipient) {
    setSelectedRecipient(recipient)
    setStep('compose')
  }

  function handleBack() {
    setSelectedRecipient(null)
    setSendError(null)
    setStep('recipient')
  }

  async function handleSend() {
    if (!selectedRecipient) return

    setSending(true)
    setSendError(null)

    try {
      const trimmedMessage = message.trim()
      await CanvasesApi.sendCanvas(canvasId, {
        recipientUserId: selectedRecipient.userId,
        message: trimmedMessage === '' ? null : trimmedMessage,
        showName,
      })
      onSent()
    } catch (cause) {
      setSendError(formatApiError(cause))
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="admin-modal-backdrop" onClick={onClose}>
      <div
        className="modal-card send-canvas-modal"
        onClick={e => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-label="Send your drawing"
      >
        {step === 'recipient' && (
          <>
            <h2>Who do you want to send this to?</h2>

            <RecipientPicker
              recipients={recipients}
              loading={loading}
              loadError={loadError}
              emptyMessage="No connections yet - ask an admin to add some."
              onSelect={handlePickRecipient}
            />

            <div className="modal-actions">
              <button type="button" className="btn-secondary" onClick={onClose}>
                Maybe later
              </button>
            </div>
          </>
        )}

        {step === 'compose' && selectedRecipient && (
          <>
            <h2>Send to {selectedRecipient.displayName}</h2>

            <div className="modal-fields">
              <label>
                Add a message (optional)
                <textarea
                  className="send-message-input"
                  value={message}
                  maxLength={MAX_MESSAGE_LENGTH}
                  onChange={e => setMessage(e.target.value)}
                  placeholder="Say something nice!"
                  rows={3}
                />
                <span className="message-counter">
                  {message.length}/{MAX_MESSAGE_LENGTH}
                </span>
              </label>

              <label className="show-name-toggle">
                <input type="checkbox" checked={showName} onChange={e => setShowName(e.target.checked)} />
                Show my name
              </label>
              <p className="show-name-hint">
                Turn this off if you've already signed the drawing and want it to be a surprise!
              </p>
            </div>

            {sendError && (
              <p className="admin-error" role="alert">
                {sendError}
              </p>
            )}

            <div className="modal-actions">
              <button
                type="button"
                className="btn-primary"
                onClick={() => void handleSend()}
                disabled={sending}
              >
                {sending ? 'Sending…' : 'Send it!'}
              </button>
              <button type="button" className="btn-secondary" onClick={handleBack} disabled={sending}>
                Back
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
