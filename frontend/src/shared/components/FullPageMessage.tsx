import './FullPageMessage.css'

type FullPageMessageProps = {
  heading: string
  message: string
  actionLabel: string
  onAction: () => void
}

export default function FullPageMessage({
  heading,
  message,
  actionLabel,
  onAction,
}: FullPageMessageProps) {
  return (
    <main className="full-page-message">
      <div className="full-page-message-card">
        <h1>{heading}</h1>
        <p>{message}</p>
        <button type="button" onClick={onAction}>
          {actionLabel}
        </button>
      </div>
    </main>
  )
}
