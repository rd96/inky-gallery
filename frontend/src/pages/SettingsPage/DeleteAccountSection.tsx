import { useState } from 'react'
import { useAuth } from '../../features/auth/useAuth'
import { SettingsApi } from '../../features/settings/api/settingsApi'
import { formatApiError } from '../../shared/api/ApiError'
import { useEscapeKey } from '../../shared/hooks/useEscapeKey'

export default function DeleteAccountSection() {
  const { refresh } = useAuth()
  const [confirming, setConfirming] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function closeConfirm() {
    if (submitting) return
    setConfirming(false)
    setError(null)
  }

  useEscapeKey(closeConfirm, confirming)

  async function handleConfirm() {
    setSubmitting(true)
    setError(null)

    try {
      await SettingsApi.deleteAccount()
      await refresh()
    } catch (cause) {
      setError(formatApiError(cause))
      setSubmitting(false)
    }
  }

  return (
    <section className="settings-section settings-danger-zone">
      <h2>Danger zone</h2>
      <p>
        Deleting your account disables it and logs you out immediately. An admin can
        re-enable it later.
      </p>
      <button type="button" className="btn-danger" onClick={() => setConfirming(true)}>
        Delete account
      </button>

      {confirming && (
        <div className="admin-modal-backdrop" onClick={closeConfirm}>
          <div
            className="modal-card"
            onClick={e => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-label="Delete account"
          >
            <h2>Delete account?</h2>
            <p className="modal-description">
              This will disable your account and log you out immediately. This can only
              be undone by an admin.
            </p>
            {error && (
              <p className="admin-error" role="alert">
                {error}
              </p>
            )}
            <div className="modal-actions">
              <button
                type="button"
                className="btn-danger"
                onClick={() => void handleConfirm()}
                disabled={submitting}
              >
                {submitting ? 'Deleting…' : 'Yes, delete my account'}
              </button>
              <button
                type="button"
                className="btn-secondary"
                onClick={closeConfirm}
                disabled={submitting}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}
