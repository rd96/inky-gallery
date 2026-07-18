import { useState } from 'react'
import {
  buildActivationLink,
  redactedActivationLink,
} from '../../features/admin/activationLink'

type ActivationLinkCardProps = {
  username: string
  activationToken: string
  expiresAt?: string
  onDone: () => void
}

export default function ActivationLinkCard({
  username,
  activationToken,
  expiresAt,
  onDone,
}: ActivationLinkCardProps) {
  const [copyFailed, setCopyFailed] = useState(false)
  const link = buildActivationLink(activationToken)

  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(link)
      setCopyFailed(false)
    } catch {
      setCopyFailed(true)
    }
  }

  return (
    <div className="modal-card" onClick={e => e.stopPropagation()}>
      <h2>Ready to activate</h2>
      <p className="modal-description">
        Share this link with <strong>{username}</strong>
        {expiresAt && <> — it expires {new Date(expiresAt).toLocaleString()}</>}.
      </p>
      <code className="activation-link-redacted">
        {copyFailed ? link : redactedActivationLink()}
      </code>
      {copyFailed && (
        <p className="admin-error" role="alert">
          Couldn't copy automatically — the link above is shown in full, select
          and copy it manually.
        </p>
      )}
      <div className="modal-actions">
        <button type="button" className="btn-primary" onClick={() => void handleCopy()}>
          Copy link
        </button>
        <button type="button" className="btn-secondary" onClick={onDone}>
          Done
        </button>
      </div>
    </div>
  )
}
