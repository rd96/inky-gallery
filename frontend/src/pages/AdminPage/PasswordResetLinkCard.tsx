import { useEffect, useRef, useState } from 'react'
import {
  buildPasswordResetLink,
  redactedPasswordResetLink,
} from '../../features/admin/passwordResetLink'

type PasswordResetLinkCardProps = {
  username: string
  passwordResetToken: string
  expiresAt?: string
  onDone: () => void
}

const COPIED_RESET_MS = 1800

export default function PasswordResetLinkCard({
  username,
  passwordResetToken,
  expiresAt,
  onDone,
}: PasswordResetLinkCardProps) {
  const [copyFailed, setCopyFailed] = useState(false)
  const [copied, setCopied] = useState(false)
  const resetTimeoutRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined)
  const link = buildPasswordResetLink(passwordResetToken)

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
      <h2>Ready to reset password</h2>
      <p className="modal-description">
        Share this link with <strong>{username}</strong>
        {expiresAt && <> — it expires {new Date(expiresAt).toLocaleString()}</>}.
      </p>
      <button
        type="button"
        className={
          copied ? 'copy-link-field copy-link-field--copied' : 'copy-link-field'
        }
        onClick={() => void handleCopy()}
      >
        <code className="copy-link-redacted">
          {copyFailed ? link : redactedPasswordResetLink()}
        </code>
        <span className="copy-link-hint" aria-hidden="true">
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
