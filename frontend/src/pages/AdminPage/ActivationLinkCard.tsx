import { useEffect, useRef, useState } from 'react'
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

const COPIED_RESET_MS = 1800

export default function ActivationLinkCard({
  username,
  activationToken,
  expiresAt,
  onDone,
}: ActivationLinkCardProps) {
  const [copyFailed, setCopyFailed] = useState(false)
  const [copied, setCopied] = useState(false)
  const resetTimeoutRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined)
  const link = buildActivationLink(activationToken)

  useEffect(() => () => clearTimeout(resetTimeoutRef.current), [])

  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(link)
      setCopyFailed(false)
      setCopied(true)
      clearTimeout(resetTimeoutRef.current)
      resetTimeoutRef.current = setTimeout(() => setCopied(false), COPIED_RESET_MS)
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
      <button
        type="button"
        className={
          copied ? 'activation-link-field activation-link-field--copied' : 'activation-link-field'
        }
        onClick={() => void handleCopy()}
      >
        <code className="activation-link-redacted">
          {copyFailed ? link : redactedActivationLink()}
        </code>
        <span className="activation-link-hint" aria-hidden="true">
          {copied ? '✓ Copied!' : 'Click to copy'}
        </span>
      </button>
      {copyFailed && (
        <p className="admin-error" role="alert">
          Couldn't copy automatically — the link above is shown in full, select
          and copy it manually.
        </p>
      )}
      <div className="modal-actions">
        <button type="button" className="btn-secondary" onClick={onDone}>
          Done
        </button>
      </div>
    </div>
  )
}
