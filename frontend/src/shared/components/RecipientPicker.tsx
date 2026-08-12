import { useState } from 'react'
import type { Recipient } from '../../features/recipients/types'
import './RecipientPicker.css'

type RecipientPickerProps = {
  recipients: Recipient[]
  loading: boolean
  loadError: string | null
  emptyMessage: string
  onSelect: (recipient: Recipient) => void
}

export default function RecipientPicker({
  recipients,
  loading,
  loadError,
  emptyMessage,
  onSelect,
}: RecipientPickerProps) {
  const [search, setSearch] = useState('')

  const filteredRecipients = recipients.filter(recipient =>
    recipient.displayName.toLowerCase().includes(search.trim().toLowerCase()),
  )

  if (loading) return <p>Finding your people…</p>

  if (loadError) {
    return (
      <p className="admin-error" role="alert">
        {loadError}
      </p>
    )
  }

  if (recipients.length === 0) return <p className="recipient-empty">{emptyMessage}</p>

  return (
    <>
      <input
        type="text"
        className="recipient-search"
        placeholder="Type a name..."
        value={search}
        onChange={e => setSearch(e.target.value)}
        autoFocus
      />

      <ul className="recipient-list">
        {filteredRecipients.map(recipient => (
          <li key={recipient.userId}>
            <button type="button" className="recipient-option" onClick={() => onSelect(recipient)}>
              <span className="recipient-avatar" aria-hidden="true">
                {recipient.displayName.charAt(0).toUpperCase()}
              </span>
              <span className="recipient-names">
                <span className="recipient-display-name">{recipient.displayName}</span>
                <span className="recipient-username">@{recipient.username}</span>
              </span>
            </button>
          </li>
        ))}
      </ul>

      {filteredRecipients.length === 0 && <p className="recipient-empty">No one found for that name</p>}
    </>
  )
}
